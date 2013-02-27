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
package org.xbib.tools.indexer.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.namespace.QName;

import org.elasticsearch.client.support.ingest.transport.TransportClientIngestSupport;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.InputService;
import org.xbib.io.file.Finder;
import org.xbib.io.util.URIUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.XmlReader;
import org.xbib.rdf.io.xml.XmlResourceHandler;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

public final class EZB extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(EZB.class.getName());
    private final static String lf = System.getProperty("line.separator");
    private final static AtomicLong fileCounter = new AtomicLong(0L);
    private static Queue<URI> input;
    private static OptionSet options;
    private final SimpleResourceContext resourceContext = new SimpleResourceContext();
    private ElementOutput out;
    private boolean done = false;
    private static String index;
    private static String type;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.xml");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("overwrite").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + EZB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads (optional, default: 1)"
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --overwrite <bool>     should index be deleted befefore indexing (optional, default: false)"
                        );
                System.exit(1);
            }
            
            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {},  threads = {}", input, threads);

            URI esURI = URI.create(options.valueOf("elasticsearch").toString());
            index = options.valueOf("index").toString();
            type = options.valueOf("type").toString();
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            boolean overwrite = (Boolean) options.valueOf("overwrite");            
            
            final TransportClientIngestSupport es = new TransportClientIngestSupport();
            
            // we always delete index first, and we disable date detection
            es.maxBulkActions(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .newClient(esURI)
                    .index(index)
                    .type(type)
                    .waitForHealthyCluster()
                    .deleteIndex(overwrite)
                    .dateDetection(false)
                    .newIndex();

            final ElasticsearchResourceSink<ResourceContext, Resource> sink =
                    new ElasticsearchResourceSink(es);

            long t0 = System.currentTimeMillis();
            new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new EZB(sink);
                        }
                    }).execute().shutdown();
            long t1 = System.currentTimeMillis();

            double dps = sink.getCounter() * 1000 /  (t1-t0);
            logger.info("Complete. {} files, {} docs, {} ms ({} dps)", fileCounter, sink.getCounter(), t1-t0, dps);

            es.shutdown();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public EZB(ElementOutput out) {
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
        URI uri = input.poll();
        done = uri == null;
        if (done) {
            return fileCounter;
        }
        try {
            AbstractXmlHandler handler = new Handler(resourceContext)
                    .setListener(new ResourceBuilder())
                    .setDefaultNamespace("ezb", "http://ezb.uni-regensburg.de/ezeit/");
            InputStream in = InputService.getInputStream(uri);
            new XmlReader()
                    .setNamespaces(false)
                    .setHandler(handler)
                    .parse(in);
            in.close();
            fileCounter.incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while getting next document: " + ex.getMessage(), ex);
        }
        return fileCounter;
    }

    class Handler extends XmlResourceHandler {

        public Handler(ResourceContext ctx) {
            super(ctx);
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            if ("license_entry_id".equals(name.getLocalPart()) && identifier == null) {
                IRI id = new IRI().scheme("urn").host(index).query(type).fragment(value).build();
                resourceContext.resource().id(id);
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "license_set".equals(name.getLocalPart());
        }

        @Override
        public void closeResource() {
            try { 
                out.output(resourceContext);
            } catch (IOException e ) {
                logger.error(e.getMessage(), e);
            }
            super.closeResource();
        }

        @Override
        public boolean skip(QName name) {
            boolean skip =
                    "ezb-export".equals(name.getLocalPart())
                    || "release".equals(name.getLocalPart())
                    || "version".equals(name.getLocalPart());
            return skip;
        }
        
        @Override
        protected Object toLiteral(QName name, String content) {
            switch (name.getLocalPart()) {
                case "reference_url":
                case "readme_url":
                    return URIUtil.decode(content, "UTF-8");
                case "zdbid":                
                    return content.replaceAll("\\-", "");
            }
            return super.toLiteral(name, content);
        }
    }

    class ResourceBuilder implements TripleListener {

        @Override
        public ResourceBuilder newIdentifier(IRI identifier) {
            resourceContext.resource().id(identifier);
            return this;
        }

        @Override
        public ResourceBuilder triple(Triple triple) {
            resourceContext.resource().add(triple);
            return this;
        }
    }
}
