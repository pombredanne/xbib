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

import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elements.marc.dialects.pica.PicaBuilder;
import org.xbib.elements.marc.dialects.pica.PicaBuilderFactory;
import org.xbib.elements.marc.dialects.pica.PicaElementMapper;
import org.xbib.elements.ElementOutput;
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
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.FormatUtil;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public final class BibdatZDBConverter extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(BibdatZDBConverter.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final static AtomicLong outputCounter = new AtomicLong(0L);

    private static Queue<URI> input;

    private static OptionSet options;

    private PicaBuilderFactory factory;

    private boolean done = false;

    private static int pipelines;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.xml");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("pipelines").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("output").withOptionalArg().ofType(String.class).defaultsTo("output.nt");
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + BibdatZDBConverter.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads (optional, default: 1)"
                        + " --pipeline <n>         the number of pipeline (optional, default: 1)"
                        + " --output <name>        the output filename (default: output.nt)"
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            pipelines = (Integer)options.valueOf("pipelines");
            final Integer threads = (Integer) options.valueOf("threads");

            final String output = options.valueOf("output").toString();

            final OurElementOutput eo = new OurElementOutput(output);

            final PicaBuilderFactory factory = new PicaBuilderFactory() {
                public PicaBuilder newBuilder() {
                    return new PicaBuilder().addOutput(eo);
                }
            };

            long t0 = System.currentTimeMillis();
            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new BibdatZDBConverter(factory);
                        }
                    }).execute();

            long t1 = System.currentTimeMillis();
            long docs = outputCounter.get();
            long bytes = eo.getVolumeInBytes();
            double dps = docs * 1000.0 / (double)(t1 - t0);
            double avg = bytes / (docs + 1); // avoid div by zero
            double mbps = (bytes * 1000.0 / (double)(t1 - t0)) / (1024.0 * 1024.0) ;
            String t = TimeValue.timeValueMillis(t1 - t0).format();
            String byteSize = FormatUtil.convertFileSize(bytes);
            String avgSize = FormatUtil.convertFileSize(avg);
            NumberFormat formatter = NumberFormat.getNumberInstance();
            logger.info("Indexing complete. {} files, {} docs, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
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
            eo.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    BibdatZDBConverter(PicaBuilderFactory factory) {
        this.factory = factory;
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
                            return Normalizer.normalize(value, Normalizer.Form.NFC);
                        }
                    })
                    .addListener(mapper);

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

    static class OurElementOutput implements ElementOutput<ResourceContext,Resource> {

        File f;
        FileWriter fw;
        NTripleWriter writer;

        OurElementOutput(String filename) throws IOException {
            this.f = new File(filename);
            this.fw = new FileWriter(f);
            this.writer =  new NTripleWriter()
                    .output(fw)
                    .setNullPredicate(
                            IRI.builder().scheme("http").host("xbib.org").path("/adr").build());
        }

        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public void enabled(boolean enabled) {
        }

        @Override
        public void output(ResourceContext context, ContentBuilder contentBuilder) throws IOException {
            IRI id = IRI.builder().scheme("http").host("xbib.org").path("/adr")
                    .fragment(context.resource().id().getFragment()).build();
            context.resource().id(id);
            writer.write(context.resource());
            outputCounter.incrementAndGet();
        }

        @Override
        public long getCounter() {
            return outputCounter.get();
        }

        public long getVolumeInBytes() {
            return f.length();
        }

        public void shutdown() throws IOException {
            fw.close();
        }
    }

}
