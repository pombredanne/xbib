
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
package org.xbib.tools.convert;

import org.xbib.elements.ElementOutput;
import org.xbib.elements.marc.dialects.mab.MABElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABElementBuilderFactory;
import org.xbib.elements.marc.dialects.mab.MABElementMapper;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.file.Finder;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.dialects.MarcXmlTarReader;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Converter tool for Hochschulbibliothekszentrum (HBZ) MAB data in MarcXml TAR clobs
 *
 * @author Jörg Prante <joergprante@gmail.com>
 */
public final class HBZConverter extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(HBZConverter.class.getSimpleName());

    private final static String lf = System.getProperty("line.separator");

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final static AtomicLong outputCounter = new AtomicLong(0L);

    private static Queue<URI> input;

    private static String elements;

    private static int pipelines;

    private static boolean detect;

    private boolean done;

    private final static Set<String> unknownKeys = Collections.synchronizedSet(new TreeSet());

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required();
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("overwrite").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("elements").withRequiredArg().ofType(String.class).required().defaultsTo("mab/hbz/dialect");
                    accepts("pipelines").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("detect").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                }
            };
            OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + HBZConverter.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads for import (optional, default: 1)"
                        + " --elements <name>      element set (optional, default: marc)"
                        + " --pipelines <n>        number of pipelines (optional, default: number of cpu cores)"
                        + " --detect <bool>        detect unknown keys (optional, default: false)"
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString())
                    .find(options.valueOf("path").toString())
                    .getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("number of input files = {}, worker threads = {}", input.size(), threads);

            // configure element processing
            pipelines = (Integer)options.valueOf("pipelines");
            elements = options.valueOf("elements").toString();
            detect = (Boolean)options.valueOf("detect");

            long t0 = System.currentTimeMillis();

            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new HBZConverter();
                        }
                    }).execute();

            long t1 = System.currentTimeMillis();

            long docs = outputCounter.get();
            logger.info("Indexing complete. {} files, {} docs, {} ms",
                    fileCounter,
                    docs,
                    (t1-t0));

            service.shutdown();

            // output of mapper analysis

            if (detect) {
                StringBuilder sb = new StringBuilder();
                for (String key :  unknownKeys) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append("\n").append("\"").append(key).append("\"");
                }
                logger.info("detected unknown keys={}", sb.toString());
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private HBZConverter() {
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
        final URI uri = input.poll();
        done = uri == null;
        if (done) {
            return fileCounter;
        }
        try {
            logger.info("starting {} elements '{}'", uri, elements);
            final MABElementMapper mapper = new MABElementMapper(elements)
                    .pipelines(pipelines)
                    .detectUnknownKeys(detect)
                    .start(buildFactory);
            final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                    .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                        @Override
                        public String transform(String value) {
                            return value;
                        }
                    })
                    .addListener(mapper);
            // set up TAR reader
            final MarcXmlTarReader reader = new MarcXmlTarReader()
                            .setURI(uri)
                            .setListener(kv);
            while (reader.hasNext()) {
                reader.next();
            }
            reader.close();
            if (detect) {
                unknownKeys.addAll(mapper.unknownKeys());
            }
            mapper.close();
            fileCounter.incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while reading document: " + ex.getMessage(), ex);
            done = true;
        }
        return fileCounter;
    }

    final MABElementBuilderFactory buildFactory = new MABElementBuilderFactory() {
        public MABElementBuilder newBuilder() {
            return new MABElementBuilder()
                    .addOutput(new OurElementOutput());
        }
    };

    final class OurElementOutput implements ElementOutput<ResourceContext,Resource> {

        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public void enabled(boolean enabled) {
        }

        @Override
        public void output(ResourceContext context, ContentBuilder contentBuilder) throws IOException {
            if (context.resource().id() != null) {
                if (logger.isDebugEnabled()) {
                    try {
                        StringWriter sw = new StringWriter();
                        new TurtleWriter().output(sw).write(context.resource());
                        logger.info(sw.toString());
                    } catch (IOException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            } else {
                logger.warn("no resource ID found");
            }
        }

        @Override
        public long getCounter() {
            return outputCounter.get();
        }
    }

}
