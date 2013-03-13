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
import java.io.InputStreamReader;
import java.net.URI;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.elasticsearch.client.support.ingest.ClientIngest;
import org.elasticsearch.client.support.ingest.transport.MockTransportClientIngest;
import org.elasticsearch.client.support.ingest.transport.TransportClientIngest;
import org.elasticsearch.client.support.ingest.transport.TransportClientIngestSupport;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elements.marc.extensions.pica.PicaBuilder;
import org.xbib.elements.marc.extensions.pica.PicaBuilderFactory;
import org.xbib.elements.marc.extensions.pica.PicaElementMapper;
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
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.xml.DNBPICAXmlReader;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.FormatUtil;
import org.xml.sax.InputSource;

public final class BibdatZDB extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(BibdatZDB.class.getName());
    private final static String lf = System.getProperty("line.separator");
    private final static AtomicLong fileCounter = new AtomicLong(0L);
    private final static AtomicLong outputCounter = new AtomicLong(0L);
    private static Queue<URI> input;
    private ElementOutput output;
    private static OptionSet options;
    private boolean done = false;
    private static String index;
    private static String type;
    private static int pipelines;
    private static int buffersize;
    private static boolean mock;
    private static boolean detect;

    public BibdatZDB(ElementOutput output) {
        this.output = output;
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

            PicaElementMapper mapper = new PicaElementMapper("pica/zdb/bib")
                    .pipelines(pipelines)
                    .detectUnknownKeys(true)
                    .start(factory);

            MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                        @Override
                        public String transform(String value) {
                            // DNB Pica contains denormalized UTF-8, use
                            // compatibility composing (best for search engines)
                            return Normalizer.normalize(
                                    value,
                                    Normalizer.Form.NFKC);
                        }
                    })
                    .addListener(mapper);
                    /*.addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                        @Override
                        public void begin() {
                            logger.debug("begin object");
                        }

                        @Override
                        public void keyValue(FieldCollection key, String value) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("begin");
                                for (Field f : key) {
                                    logger.debug("tag={} ind={} subf={} data={}",
                                            f.tag(), f.indicator(), f.subfieldId(), f.data());
                                }
                                logger.debug("end");
                            }
                        }

                        @Override
                        public void end() {
                            logger.debug("end object");
                        }

                        @Override
                        public void end(Object info) {
                            logger.debug("end object (info={})", info);
                        }
                    });*/

            InputStream in = InputService.getInputStream(uri);
            InputSource source = new InputSource(new InputStreamReader(in, "UTF-8"));
            new DNBPICAXmlReader(source).setListener(kv).parse();
            in.close();
            mapper.close();

            logger.info("detected unknown elements = {}",
                    mapper.unknownKeys());

            fileCounter.incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while getting next document: " + ex.getMessage(), ex);
        }
        return fileCounter;
    }

    private final PicaBuilderFactory factory = new PicaBuilderFactory() {
        public PicaBuilder newBuilder() {
            return new PicaBuilder().addOutput(new OurElementOutput());
        }
    };

    final class OurElementOutput implements ElementOutput<ResourceContext> {

        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public void enabled(boolean enabled) {
        }

        @Override
        public void output(ResourceContext context) throws IOException {
            // set index/type adressing via resource ID
            context.resource().id(new IRI().host(index).query(type)
                    .fragment(context.resource().id().getFragment()).build());
            output.output(context);
            outputCounter.incrementAndGet();
        }

        @Override
        public long getCounter() {
            return outputCounter.get();
        }
    }

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
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("pipelines").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("buffersize").withRequiredArg().ofType(Integer.class).defaultsTo(8192);
                    accepts("detect").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + BibdatZDB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads (optional, default: 1)"
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {}, threads = {}", input, threads);

            URI esURI = URI.create(options.valueOf("elasticsearch").toString());
            index = options.valueOf("index").toString();
            type = options.valueOf("type").toString();
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");

            mock = (Boolean)options.valueOf("mock");
            pipelines = (Integer)options.valueOf("pipelines");
            buffersize = (Integer)options.valueOf("buffersize");
            detect = (Boolean)options.valueOf("detect");

            final TransportClientIngestSupport es = mock ?
                    new MockTransportClientIngest() :
                    new TransportClientIngestSupport();

            es.maxBulkActions(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .newClient(esURI)
                    .waitForHealthyCluster();

            logger.info("creating new index ...");
            es.setIndex(index)
                    .setType(type)
                    .dateDetection(false)
                    .newIndex(false);
            logger.info("... new index created");

            logger.info("creating new type ...");
            es.newType();
            logger.info("... new type done");

            final ElasticsearchResourceSink<ResourceContext, Resource> sink =
                    new ElasticsearchResourceSink(es);

            long t0 = System.currentTimeMillis();
            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new BibdatZDB(sink);
                        }
                    }).execute();

            long t1 = System.currentTimeMillis();
            long docs = outputCounter.get();
            long bytes = es.getVolumeInBytes();
            double dps = docs * 1000.0 / (double)(t1 - t0);
            double avg = bytes / (docs + 1.0); // avoid div by zero
            double mbps = (bytes * 1000.0 / (double)(t1 - t0)) / (1024.0 * 1024.0) ;
            String t = TimeValue.timeValueMillis(t1 - t0).format();
            String byteSize = FormatUtil.convertFileSize(bytes);
            String avgSize = FormatUtil.convertFileSize(avg);
            NumberFormat formatter = NumberFormat.getNumberInstance();
            logger.info("Indexing complete. {} files, {} docs, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
                    fileCounter, docs, t, (t1-t0), byteSize, bytes,
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


}
