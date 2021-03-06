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
package org.xbib.elasticsearch.tools.ingest;

import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.ResourceSink;
import org.xbib.elasticsearch.support.client.IngestClient;
import org.xbib.elasticsearch.support.client.MockIngestClient;
import org.xbib.elements.ElementOutput;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.file.Finder;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.dialects.MarcXmlTarReader;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.FormatUtil;

import java.io.IOException;
import java.net.URI;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Elasticsearch indexer for Zeitschriftendatenbank (ZDB) MARC-XML tar archive files
 */
public final class ZDBFromMarcXmlTar extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(ZDBFromMarcXmlTar.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final static AtomicLong outputCounter = new AtomicLong(0L);

    private static Queue<URI> input;

    private static String index;

    private static String type;

    private static String elements;

    private static int pipelines;

    private static boolean detect;

    private ElementOutput output;

    private boolean done;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("shards").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("1208zdblokutf8.mrc");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("overwrite").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("elements").withRequiredArg().ofType(String.class).required().defaultsTo("marc");
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("pipelines").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("detect").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                }
            };
            OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + ZDBFromMarcXmlTar.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --shards <n>           Elasticsearch number of shards" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads for import (optional, default: 1)"
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --elements <name>      element set (optional, default: marc)"
                        + " --mock <bool>          dry run of indexing (optional, default: false)"
                        + " --pipelines <n>        number of pipelines (optional, default: number of cpu cores)"
                        + " --detect <bool>        detect unknown keys (optional, default: false)"
                );
                System.exit(1);
            }

            // the harvested files must be sorted by path name
            input = new Finder(options.valueOf("pattern").toString())
                    .find(options.valueOf("path").toString())
                    .pathSorted()
                    .getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("input = {}, threads = {}", input, threads);

            URI esURI = URI.create(options.valueOf("elasticsearch").toString());
            index = options.valueOf("index").toString();
            type = options.valueOf("type").toString();
            String shards = options.valueOf("shards").toString();
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            elements = options.valueOf("elements").toString();
            boolean mock = (Boolean) options.valueOf("mock");
            pipelines = (Integer) options.valueOf("pipelines");
            detect = (Boolean) options.valueOf("detect");

            final IngestClient es = mock ? new MockIngestClient() : new IngestClient();
            es.maxBulkActions(maxbulkactions)
                            .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                            .newClient(esURI)
                            .waitForCluster()
                            .setIndex(index)
                            .setType(type)
                            .shards(Integer.parseInt(shards))
                            .replica(0)
                            .newIndex()
                            .startBulk();

            final ResourceSink<ResourceContext, Resource> sink = new ResourceSink(es);

            long t0 = System.currentTimeMillis();
            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new ZDBFromMarcXmlTar(sink);
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
            es.stopBulk().shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private ZDBFromMarcXmlTar(ElementOutput output) {
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

            final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                        @Override
                        public String transform(String value) {
                            return Normalizer.normalize(value, Normalizer.Form.NFC);
                        }
                    })
                    .addListener(mapper);

            final MarcXmlTarReader reader = new MarcXmlTarReader()
                    .setURI(uri)
                    .setListener(kv);
            while (reader.hasNext()) {
                reader.next();
            }
            reader.close();
            fileCounter.incrementAndGet();
            mapper.close();
        } catch (Exception ex) {
            logger.error("error while getting next document: " + ex.getMessage(), ex);
        }
        return fileCounter;
    }

    final MARCElementBuilderFactory buildFactory = new MARCElementBuilderFactory() {
        public MARCElementBuilder newBuilder() {
            MARCElementBuilder builder = new MARCElementBuilder()
                    .addOutput(new OurElementOutput());
            return builder;
        }
    };

    final class OurElementOutput implements ElementOutput<ResourceContext, Resource> {

        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public void enabled(boolean enabled) {
        }

        @Override
        public void output(ResourceContext context, ContentBuilder contentBuilder) throws IOException {
            if (!context.resource().isEmpty()) {
                IRI id = IRI.builder().scheme("http")
                        .host(index)
                        .query(type)
                        .fragment(Long.toString(outputCounter.incrementAndGet())).build();
                context.resource().id(id);
                output.output(context, contentBuilder);
            }
        }

        @Override
        public long getCounter() {
            return outputCounter.get();
        }
    }

}
