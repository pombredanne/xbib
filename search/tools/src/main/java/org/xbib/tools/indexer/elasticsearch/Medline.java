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

import org.elasticsearch.client.support.ingest.transport.TransportClientIngest;
import org.elasticsearch.client.support.ingest.transport.TransportClientIngestSupport;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.InputService;
import org.xbib.io.file.Finder;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.XmlReader;
import org.xbib.rdf.io.xml.XmlResourceHandler;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public final class Medline extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(Medline.class.getName());
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
                    accepts("es").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("medline*.xml.gz");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("bulksize").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("bulks").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for ElasticsearchMedlineIndexer");
                System.err.println(" --help                 print this help message");
                System.err.println(" --es <uri>             Elasticesearch URI");
                System.err.println(" --index <index>        Elasticsearch index name");
                System.err.println(" --type <type>          Elasticsearch type name");
                System.err.println(" --path <path>          a file path from where the input files are recursively collected (required)");
                System.err.println(" --pattern <pattern>    a regex for selecting matching file names for input (required)");
                System.err.println(" --threads <n>          the number of threads (required, default: 1)");
                System.exit(1);
            }
            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {},  threads = {}", input, threads);

            URI uri = URI.create(options.valueOf("elasticsearch").toString());
            final TransportClientIngest es = new TransportClientIngestSupport()
                    .newClient(uri)
                    .setIndex(options.valueOf("index").toString())
                    .setType(options.valueOf("type").toString())
                    .maxBulkActions((Integer)options.valueOf("bulksize"))
                    .maxConcurrentBulkRequests((Integer)options.valueOf("bulks"));

            final ElasticsearchResourceSink sink = new ElasticsearchResourceSink(es);

            new ImportService().threads(threads).factory(
                    new ImporterFactory() {

                        @Override
                        public Importer newImporter() {
                            return new Medline(sink);
                        }
                    }).execute().shutdown();
            logger.info("files indexed = {}, resources indexed = {}", fileCounter, sink.getCounter());
            es.shutdown();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public Medline(ElementOutput out) {
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
                    .setDefaultNamespace("ml", "http://www.nlm.nih.gov/medline");
            InputStream in = InputService.getInputStream(uri);
            new XmlReader()
                    .setNamespaces(false)
                    .setHandler(handler)
                    .parse(in);
            in.close();
            fileCounter.incrementAndGet();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return fileCounter;
    }

    class Handler extends XmlResourceHandler {

        public Handler(ResourceContext ctx) {
            super(ctx);
        }

        @Override
        public void closeResource() {
            super.closeResource();
            try {
                out.output(resourceContext);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "MedlineCitation".equals(name.getLocalPart());
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            if ("PMID".equals(name.getLocalPart())) {
               resourceContext.resource().id(new IRI().curi("pmid", value).build());
            }
        }

        @Override
        public boolean skip(QName name) {
            boolean skipped = "MedlineCitationSet".equals(name.getLocalPart())
                    || "MedlineCitation".equals(name.getLocalPart());
            return skipped;
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
