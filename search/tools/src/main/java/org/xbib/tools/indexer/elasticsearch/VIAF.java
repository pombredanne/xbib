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
import org.xbib.elasticsearch.support.ingest.transport.IngestClient;
import org.xbib.elasticsearch.support.ingest.transport.MockIngestClient;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.InputService;
import org.xbib.io.file.Finder;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.rdfxml.RdfXmlReader;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.FormatUtil;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * VIAF indexer to Elasticsearch
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class VIAF extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(VIAF.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final static AtomicLong docCounter = new AtomicLong(0L);

    private final static AtomicLong charCounter = new AtomicLong(0L);

    private static Queue<URI> input;

    private static OptionSet options;

    private boolean done = false;

    private BlockingQueue<String> pump;

    private int numPumps;

    private ExecutorService pumpService;

    private static ElasticsearchResourceSink sink;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(4 * Runtime.getRuntime().availableProcessors());
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.xml");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("pumps").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("translatePicaSortMarker").withOptionalArg().ofType(String.class).defaultsTo("x-viaf");
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + VIAF.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads (optional, default: 1)"
                        + " --pumps <n>            the number of pumps (optional, default: 1)"
                        + " --translatePicaSortMarker <language>     if Pica '@' sort marker should be translated to a W3C language tag, see http://www.w3.org/International/articles/language-tags/ (default: 'x-viaf')"
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            final Integer threads = (Integer) options.valueOf("threads");
            final String elasticsearch = (String) options.valueOf("elasticsearch");
            final String index = (String) options.valueOf("index");
            final String type = (String) options.valueOf("type");
            final URI esURI = URI.create(elasticsearch);
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            boolean mock = (Boolean)options.valueOf("mock");

            final IngestClient es = mock ?
                    new MockIngestClient() :
                    new IngestClient();

            es.maxBulkActions(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .setIndex(index)
                    .setType(type)
                    .newClient(esURI)
                    .waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));

            sink = new ElasticsearchResourceSink(es);

            long t0 = System.currentTimeMillis();
            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new VIAF();
                        }
                    }).execute();
            long t1 = System.currentTimeMillis();
            long docs = docCounter.get();
            long bytes = charCounter.get();
            double dps = docs * 1000.0 / (double)(t1 - t0);
            double avg = bytes / (docs + 1); // avoid div by zero
            double mbps = (bytes * 1000.0 / (double)(t1 - t0)) / (1024.0 * 1024.0) ;
            String t = FormatUtil.formatMillis(t1 - t0);
            String byteSize = FormatUtil.convertFileSize(bytes);
            String avgSize = FormatUtil.convertFileSize(avg);
            NumberFormat formatter = NumberFormat.getNumberInstance();
            logger.info("Converting complete. {} files, {} docs, {} = {} ms, {} = {} chars, {} = {} avg size, {} dps, {} MB/s",
                    fileCounter,
                    docs,
                    t,
                    (t1-t0),
                    bytes,
                    byteSize,
                    avgSize,
                    formatter.format(avg),
                    formatter.format(dps),
                    formatter.format(mbps));

            service.shutdown();

            es.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private VIAF() {
        this.numPumps = (Integer)options.valueOf("pumps");
        this.pump = new SynchronousQueue(true);
        this.pumpService = Executors.newFixedThreadPool(numPumps);
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
            InputStream in = InputService.getInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            for (int i = 0; i < numPumps; i++) {
                pumpService.submit(new VIAFPipeline(i));
            }
            String line;
            long linecounter = 0;
            while ((line = reader.readLine()) != null) {
                pump.put(line);
                linecounter++;
                if (linecounter % 10000 == 0) {
                    logger.info("{}", linecounter);
                }
            }
            in.close();
            for (int i = 0; i < numPumps; i++) {
                pump.put("|");
            }
            fileCounter.incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while getting next document: " + ex.getMessage(), ex);
        } finally {
            pumpService.shutdownNow();
            pumpService = null;
        }
        return fileCounter;
    }

    class VIAFPipeline implements Callable<Boolean> {

        OutputStream out;

        VIAFPipeline(int i) throws Exception {
        }

        @Override
        public Boolean call() throws Exception {
            try {
                while (true) {
                    String line = pump.take();
                    if ("|".equals(line)) {
                        break;
                    }
                    final ElasticBuilder builder = new ElasticBuilder(sink);
                    RdfXmlReader rdfxml = new RdfXmlReader();
                    rdfxml.setTripleListener(builder);
                    rdfxml.parse(new InputSource(new StringReader(line)));
                    builder.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            } finally {
                out.close();
            }
            return true;
        }
    }

    private class ElasticBuilder implements TripleListener {

        private final ElasticsearchResourceSink sink;

        private final ResourceContext context = new SimpleResourceContext();

        private Resource resource;

        ElasticBuilder(ElasticsearchResourceSink sink) throws IOException {
            this.sink = sink;
            resource = context.newResource();
        }

        public void close() throws IOException {
            flush();
            sink.flush();
        }

        @Override
        public TripleListener startPrefixMapping(String prefix, String uri) {
            return this;
        }

        @Override
        public TripleListener endPrefixMapping(String prefix) {
            return this;
        }

        @Override
        public ElasticBuilder newIdentifier(IRI iri) {
            flush();
            resource.id(iri);
            return this;
        }

        @Override
        public ElasticBuilder triple(Triple triple) {
            resource.add(triple);
            return this;
        }

        private void flush() {
            try {
                sink.output(context, context.contentBuilder());
            } catch (IOException e) {
                logger.error("flush failed: {}", e.getMessage(), e);
            }
            context.reset();
            resource = context.newResource();
        }

    }


}

