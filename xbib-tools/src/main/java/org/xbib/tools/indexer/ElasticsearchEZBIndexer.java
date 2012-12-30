/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.tools.indexer;

import org.xbib.elasticsearch.ElasticsearchIndexer;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.InputStreamService;
import org.xbib.io.file.Finder;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.XmlReader;
import org.xbib.rdf.io.xml.XmlResourceHandler;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public final class ElasticsearchEZBIndexer extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(ElasticsearchEZBIndexer.class.getName());
    private final static String lf = System.getProperty("line.separator");
    private final static AtomicLong fileCounter = new AtomicLong(0L);
    private static Queue<URI> input;
    private static OptionSet options;
    private final SimpleResourceContext resourceContext = new SimpleResourceContext();
    private ElementOutput out;
    private boolean done = false;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("HBZ_update_dump201250001.xml");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("bulksize").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("bulks").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + ElasticsearchEZBIndexer.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (required)" + lf
                        + " --threads <n>          the number of threads (required, default: 1)");
                System.exit(1);
            }
            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {},  threads = {}", new Object[]{input, threads});

            final ElasticsearchIndexer es = new ElasticsearchIndexer();

            es.newClient(URI.create(options.valueOf("elasticsearch").toString()), false)
                    .setIndex(options.valueOf("index").toString())
                    .setType(options.valueOf("type").toString())
                    .setBulkSize((Integer) options.valueOf("bulksize"))
                    .setMaxActiveRequests((Integer) options.valueOf("bulks"));

            final ElasticsearchResourceSink<ResourceContext, Resource> sink =
                    new ElasticsearchResourceSink(es);

            ImportService service = new ImportService().setThreads(threads).setFactory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new ElasticsearchEZBIndexer(sink);
                        }
                    }).execute();
            logger.info("files = {}, docs indexed = {}", fileCounter, sink.getCounter());
            es.flush();
            es.shutdown();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public ElasticsearchEZBIndexer(ElementOutput out) {
        this.out = out;
    }

    @Override
    public void close() throws IOException {
        input.clear();
    }

    @Override
    public boolean hasNext() {
        return !done && !input.isEmpty();
    }

    @Override
    public AtomicLong next() {
        URI uri = input.isEmpty() ? null : input.poll();
        done = (uri == null);
        if (done) {
            return fileCounter;
        }
        try {
            AbstractXmlHandler handler = new Handler(resourceContext)
                    .setListener(new ResourceBuilder())
                    .setDefaultNamespace("ezb", "http://ezb.uni-regensburg.de/ezeit/");
            XmlReader reader = new XmlReader().setHandler(handler)
                    .setNamespaces(false)
                    .parse(InputStreamService.getInputStream(uri));
            reader.close();
            fileCounter.incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while getting next document: {}", ex.getMessage(), ex);
        }
        return fileCounter;
    }

    class Handler extends XmlResourceHandler {

        public Handler(ResourceContext ctx) {
            super(ctx);
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            //logger.info("looking for ID in element {}, value = {}", name, value );
            if ("license_entry_id".equals(name.getLocalPart())) {
                resourceContext.resource().id(IRI.create("ezbid:" + value));
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            //logger.info("looking for resouce delimiter in element {}", name );
            return "license_set".equals(name.getLocalPart());
            //return false;
        }

        @Override
        public void closeResource() {
            try { 
                out.output(resourceContext);
                logger.info("resource output complete: {}", resourceContext.resource());
            } catch (IOException e ) {
                logger.error(e.getMessage(), e);
            }
            super.closeResource();
        }

        @Override
        public boolean skip(QName name) {
            boolean skipped =
                    "ezb-export".equals(name.getLocalPart())
                    || "release".equals(name.getLocalPart())
                    || "version".equals(name.getLocalPart());
            return skipped;
        }
    }

    class ResourceBuilder implements StatementListener {

        @Override
        public void newIdentifier(IRI identifier) {
            resourceContext.resource().id(identifier);
        }

        @Override
        public void statement(Statement statement) {
            resourceContext.resource().add(statement);
        }
    }
}
