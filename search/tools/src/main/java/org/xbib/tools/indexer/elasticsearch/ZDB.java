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
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import org.xbib.analyzer.marc.MARCBuilder;
import org.xbib.analyzer.marc.MARCElement;
import org.xbib.analyzer.marc.MARCElementMapper;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elasticsearch.support.ElasticsearchIndexer;
import org.xbib.elasticsearch.support.IElasticsearchIndexer;
import org.xbib.elasticsearch.support.MockElasticsearchIndexer;
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
    private static Queue<URI> input;
    private ElementOutput output;
    private static OptionSet options;
    private boolean done = false;
    private static String index;
    private static String type;
    private static String elements;


    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("1208zdblokutf8.mrc");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("overwrite").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("elements").withRequiredArg().ofType(String.class).required().defaultsTo("marc");
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + ZDB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads (optional, default: 1)"
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --elements <name>      element set (optional, default: marc)");
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
            elements = options.valueOf("elements").toString();
            Boolean mock = (Boolean)options.valueOf("mock");

            final IElasticsearchIndexer es = mock ?
                    new MockElasticsearchIndexer() :
                    new ElasticsearchIndexer()
                    .maxBulkActions(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .newClient(esURI)
                    .index(index)
                    .type(type)
                    .deleteIndex()
                    .dateDetection(false)
                    .newIndex()
                    .waitForHealthyCluster();

            // we write resources to Elasticsearch
            final ElasticsearchResourceSink<ResourceContext, Resource> sink =
                    new ElasticsearchResourceSink(es);

            // do the import
            long t0 = System.currentTimeMillis();
            new ImportService().setThreads(threads).setFactory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new ZDB(sink);
                        }
                    }).execute().shutdown();
            long t1 = System.currentTimeMillis();

            double dps = sink.getCounter() * 1000 / (t1 - t0);

            logger.info("Complete. {} files, {} docs, {} ms ({} dps)",
                    fileCounter, sink.getCounter(), t1 - t0, dps);

            es.shutdown();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public ZDB(ElementOutput output) {
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
            MARCBuilder builder = new OurMARCBuilder()
                    .addOutput(new OurElementOutput());
            MARCElementMapper mapper = new MARCElementMapper(elements)
                    .catchall(true)
                    .addBuilder(builder);
            MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    /*.transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                @Override
                public String transform(String in) {
                    // ZDB PICA UTF-8 is delivered with decomposed form (e.g. 0308 COMBINING DIAERESIS)
                    return Normalizer.normalize(in, Form.NFC);
                }
            })*/
                    .addListener(mapper);
            Iso2709Reader reader = new Iso2709Reader()
                    .setMarcXchangeListener(kv);
            reader.setProperty(Iso2709Reader.FORMAT, "MARC");
            reader.setProperty(Iso2709Reader.TYPE, "Holdings");
            reader.setProperty(Iso2709Reader.FATAL_ERRORS, false);
            reader.setProperty(Iso2709Reader.SILENT_ERRORS, true);
            InputStreamReader r = new InputStreamReader(InputService.getInputStream(uri), "UTF-8");
            InputSource source = new InputSource(r);
            reader.parse(source);
            r.close();
            fileCounter.incrementAndGet();
            logger.info("elements={}", mapper.elements());
        } catch (Exception ex) {
            logger.error("error while getting next document: " + ex.getMessage(), ex);
        }
        return fileCounter;
    }

    class OurMARCBuilder extends MARCBuilder {

        @Override
        public void build(MARCElement element, FieldCollection fields, String value) {
            if (context().resource().id() == null) {
                IRI id = new IRI().scheme("http").host(index).query(type).fragment(Long.toString(context().increment())).build();
                context().resource().id(id);
            }
        }
    }

    class OurElementOutput implements ElementOutput<ResourceContext> {

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
                logger.debug("output={}", context.resource());
            }
            output.output(context);
            context.reset();
        }

        @Override
        public long getCounter() {
            return 0;
        }
    }
}
