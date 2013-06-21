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

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elasticsearch.support.bulk.transport.MockTransportClientBulk;
import org.xbib.elasticsearch.support.bulk.transport.TransportClientBulk;
import org.xbib.elasticsearch.support.bulk.transport.TransportClientBulkSupport;
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
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.xml.AbstractXmlHandler;
import org.xbib.rdf.io.xml.AbstractXmlResourceHandler;
import org.xbib.rdf.io.xml.XmlReader;
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

/**
 * Elasticsearch indexer tool for Medline XML files
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public final class Medline extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(Medline.class.getName());

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
                    accepts("index").withRequiredArg().ofType(String.class).required().defaultsTo("medline");
                    accepts("type").withRequiredArg().ofType(String.class).required().defaultsTo("medline");
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(4 * Runtime.getRuntime().availableProcessors());
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("medline13*.xml.gz");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(4 * Runtime.getRuntime().availableProcessors());
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + Medline.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.json)" + lf
                        + " --threads <n>          the number of threads (optional, default: <num-of=cpus)"
                );
                System.exit(1);
            }
            input = new Finder((String)options.valueOf("pattern"))
                    .find((String)options.valueOf("path"))
                    .pathSorted()
                    .getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {},  threads = {}", input, threads);

            URI esURI = URI.create((String)options.valueOf("elasticsearch"));
            index = (String)options.valueOf("index");
            type = (String)options.valueOf("type");
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            boolean mock = (Boolean)options.valueOf("mock");

            final TransportClientBulk es = mock ?
                    new MockTransportClientBulk() :
                    new TransportClientBulkSupport();

            es.maxBulkActions(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .newClient(esURI)
                    .waitForHealthyCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));

            final ElasticsearchResourceSink sink = new ElasticsearchResourceSink(es);

            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {

                        @Override
                        public Importer newImporter() {
                            return new Medline(sink);
                        }
                    }).execute();
            logger.info("files indexed = {}, resources indexed = {}", fileCounter, sink.getCounter());

            service.shutdown();

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

    class Handler extends AbstractXmlResourceHandler {

        private String id = null;

        public Handler(ResourceContext ctx) {
            super(ctx);
        }

        @Override
        public void closeResource() {
            super.closeResource();
            try {
                resourceContext.resource().id(IRI.builder()
                        .scheme("http")
                        .host(index)
                        .query(type)
                        .fragment(id)
                        .build());
                out.output(resourceContext);
                id = null;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "MedlineCitation".equals(name.getLocalPart());
        }

        @Override
        public void identify(QName name, String value, IRI identifier) {
            // important: there are many occurances of PMID. We must only take the first occurance for the ID.
            if (id == null && "PMID".equals(name.getLocalPart())) {
                this.id = value;
            }
        }

        @Override
        public boolean skip(QName name) {
            boolean skipped = "MedlineCitationSet".equals(name.getLocalPart())
                    || "MedlineCitation".equals(name.getLocalPart())
                    // gives ES "unknown property" error
                    || "@Label".equals(name.getLocalPart())
                    || "@NlmCategory".equals(name.getLocalPart());
            return skipped;
        }
    }

}
