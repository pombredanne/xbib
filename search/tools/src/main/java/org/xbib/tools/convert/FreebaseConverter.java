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

import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.InputService;
import org.xbib.io.file.Finder;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.io.turtle.TurtleReader;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.FormatUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * Freebase converter. The Freebase files are in Turtle RDF.
 * This conversion generates NTriples or Turtle format.
 *
 */
public class FreebaseConverter extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(FreebaseConverter.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final static AtomicLong docCounter = new AtomicLong(0L);

    private final static AtomicLong charCounter = new AtomicLong(0L);

    private static Queue<URI> input;

    private static OptionSet options;

    private static IRI base;

    private boolean done = false;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.xml");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("format").withOptionalArg().ofType(String.class).defaultsTo("turtle");
                    accepts("output").withOptionalArg().ofType(String.class).defaultsTo("output.ttl");
                    accepts("base").withRequiredArg().ofType(String.class).required().defaultsTo("http://freebase.com/");
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + FreebaseConverter.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads (optional, default: 1)"
                        + " --format 'turtle'|'ntriples'             the output format (default: turtle)"
                        + " --output <name>                          the output filename (default: output.ttl)"
                        + " --base <IRI>           a base IRI for Turtle to resolve against (required, default: http://freebase.com/)" + lf
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString())
                    .find(options.valueOf("path")
                    .toString())
                    .getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            base = IRI.create((String)options.valueOf("base"));

            long t0 = System.currentTimeMillis();
            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new FreebaseConverter();
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

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private FreebaseConverter() {
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
            String output = (String)options.valueOf("output");
            if (fileCounter.get() > 0) {
                output += "." + fileCounter.get();
            }
            if (!output.endsWith(".gz")) {
                output = output + ".gz";
            }
            OutputStream out =  new GZIPOutputStream(new FileOutputStream(output)){
                {
                    def.setLevel(Deflater.BEST_COMPRESSION);
                }
            };
            NTripleWriter writer = new NTripleWriter()
                    .output(out);


            new TurtleReader(base)
                    .setTripleListener(writer)
                    .parse(in);
            in.close();
            fileCounter.incrementAndGet();
        } catch (Exception ex) {
            logger.error("error while parsing from stream: " + ex.getMessage(), ex);
        }
        return fileCounter;
    }

}

