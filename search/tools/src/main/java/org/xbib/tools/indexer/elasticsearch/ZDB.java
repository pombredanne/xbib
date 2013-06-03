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
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.support.ingest.transport.MockTransportClientIngest;
import org.xbib.elasticsearch.support.ingest.transport.TransportClientIngest;
import org.xbib.elasticsearch.support.ingest.transport.TransportClientIngestSupport;
import org.xbib.elements.marc.MARCBuilder;
import org.xbib.elements.marc.MARCBuilderFactory;
import org.xbib.elements.marc.MARCElement;
import org.xbib.elements.marc.MARCElementMapper;
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
import org.xbib.marc.FieldCollection;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.FormatUtil;
import org.xml.sax.InputSource;

/**
 * Elasticsearch indexer tool for Zeitschriftendatenbank (ZDB)
 *
 * @author Jörg Prante <joergprante@gmail.com>
 */
public final class ZDB extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(ZDB.class.getName());
    private final static String lf = System.getProperty("line.separator");
    private final static AtomicLong fileCounter = new AtomicLong(0L);
    private final static AtomicLong outputCounter = new AtomicLong(0L);
    private static Queue<URI> input;
    private static OptionSet options;
    private static String index;
    private static String type;
    private static String shards;
    private static String replica;
    private static String elements;
    private static int pipelines;
    private static int buffersize;
    private static boolean mock;
    private static boolean detect;
    private ElementOutput output;
    private boolean done;

    public ZDB(ElementOutput output) {
        this.output = output;
        this.done = false;
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
            final MARCElementMapper mapper = new MARCElementMapper(elements)
                    .pipelines(pipelines)
                    .detectUnknownKeys(detect)
                    .start(buildFactory);

            logger.info("mapper map={}", mapper.map().size());

            final Charset UTF8 = Charset.forName("UTF-8");
            final Charset ISO88591 = Charset.forName("ISO-8859-1");

            final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                        @Override
                        public String transform(String value) {
                            return Normalizer.normalize(new String(value.getBytes(ISO88591), UTF8),
                                    Normalizer.Form.NFKC);
                        }
                    })
                    .addListener(mapper);
            final Iso2709Reader reader = new Iso2709Reader()
                    .setMarcXchangeListener(kv);
            reader.setProperty(Iso2709Reader.FORMAT, "MARC");
            if ("marc/holdings".equals(elements)) {
                reader.setProperty(Iso2709Reader.TYPE, "Holdings");
            }
            reader.setProperty(Iso2709Reader.FATAL_ERRORS, false);
            reader.setProperty(Iso2709Reader.SILENT_ERRORS, true);
            reader.setProperty(Iso2709Reader.BUFFER_SIZE, buffersize);
            InputStreamReader r = new InputStreamReader(InputService.getInputStream(uri), ISO88591);
            InputSource source = new InputSource(r);
            reader.parse(source);
            r.close();
            fileCounter.incrementAndGet();
            logger.info("unknown keys={}", mapper.unknownKeys());
            mapper.close();
        } catch (Exception ex) {
            logger.error("error while getting next document: " + ex.getMessage(), ex);
        }
        return fileCounter;
    }

    final MARCBuilderFactory buildFactory = new MARCBuilderFactory() {
        public MARCBuilder newBuilder() {
            MARCBuilder builder = new OurMARCBuilder()
                    .addOutput(new OurElementOutput());
            return builder;
        }
    };

    final class OurMARCBuilder extends MARCBuilder {

        @Override
        public void build(MARCElement element, FieldCollection fields, String value) {
            if (context().resource().id() == null) {
                IRI id = IRI.builder().scheme("http")
                        .host(index)
                        .query(type)
                        .fragment(Long.toString(context().increment())).build();
                context().resource().id(id);
            }
        }
    }

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
            if (logger.isDebugEnabled()) {
                logger.debug("context={} resource size={}", context.asMap(), context.resource().size());
            }
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
                    accepts("shards").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("replica").withRequiredArg().ofType(Integer.class).defaultsTo(0);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("1208zdblokutf8.mrc");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("overwrite").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("elements").withRequiredArg().ofType(String.class).required().defaultsTo("marc");
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("pipelines").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("buffersize").withRequiredArg().ofType(Integer.class).defaultsTo(8192);
                    accepts("detect").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + ZDB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --shards <n>           Elasticsearch number of shards" + lf
                        + " --replica <n>          Elasticsearch number of replica" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads for import (optional, default: 1)"
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --elements <name>      element set (optional, default: marc)"
                        + " --mock <bool>          dry run of indexing (optional, default: false)"
                        + " --pipelines <n>        number of pipelines (optional, default: number of cpu cores)"
                        + " --buffersize <n>       buffer size in chars for reads (optional, default: 8192)"
                        + " --detect <bool>        detect unknown keys (optional, default: false)"
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {}, threads = {}", input, threads);

            URI esURI = URI.create(options.valueOf("elasticsearch").toString());
            index = options.valueOf("index").toString();
            type = options.valueOf("type").toString();
            shards = options.valueOf("shards").toString();
            replica = options.valueOf("replica").toString();
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            elements = options.valueOf("elements").toString();
            mock = (Boolean)options.valueOf("mock");
            pipelines = (Integer)options.valueOf("pipelines");
            buffersize = (Integer)options.valueOf("buffersize");
            detect = (Boolean)options.valueOf("detect");

            final TransportClientIngest es = mock ?
                    new MockTransportClientIngest() :
                    new TransportClientIngestSupport()
                            .maxBulkActions(maxbulkactions)
                            .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                            .newClient(esURI)
                            .waitForHealthyCluster()
                            .setIndex(index)
                            .setType(type)
                            .setting("index.number_of_shards", shards)
                            .setting("index.number_of_replicas", "0")
                            .dateDetection(false)
                            .newIndex()
                            .startBulkMode();

            // we write RDF resources to Elasticsearch
            final ElasticsearchResourceSink<ResourceContext, Resource> sink = new ElasticsearchResourceSink(es);

            // do the import
            long t0 = System.currentTimeMillis();

            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new ZDB(sink);
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
