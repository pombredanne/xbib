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

import org.xbib.date.DateUtil;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.InputService;
import org.xbib.io.file.Finder;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.io.rdfxml.RdfXmlReader;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.FormatUtil;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.FileOutputStream;
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
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;


public class VIAF extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(VIAF.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final static AtomicLong docCounter = new AtomicLong(0L);

    private final static AtomicLong charCounter = new AtomicLong(0L);

    private static Queue<URI> input;

    private static OptionSet options;

    private String output;

    private boolean done = false;

    private BlockingQueue<String> pump;

    private int numPumps;

    private ExecutorService pumpService;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.xml");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("pumps").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("detect").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("format").withOptionalArg().ofType(String.class).defaultsTo("turtle");
                    accepts("output").withOptionalArg().ofType(String.class).defaultsTo("output.ttl");
                    accepts("translatePicaSortMarker").withOptionalArg().ofType(String.class).defaultsTo("x-viaf");
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + BibdatZDB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads (optional, default: 1)"
                        + " --pumps <n>            the number of pumps (optional, default: 1)"
                        + " --format 'turtle'|'ntriples'             the output format (default: turtle)"
                        + " --output <name>                          the output filename (default: output.ttl)"
                        + " --translatePicaSortMarker <language>     if Pica '@' sort marker should be translated to a W3C language tag, see http://www.w3.org/International/articles/language-tags/ (default: 'de-DE-x-rak')"
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

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

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
    VIAF() {
        this.output = (String)options.valueOf("output");
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

        VIAFPipeline(int i) throws Exception{
            String fileName = DateUtil.today() + "_" + i + "_" + fileCounter.get() + "_" + output + ".gz";
            FileOutputStream fout = new FileOutputStream(fileName);
            this.out = new GZIPOutputStream(fout){
                {
                    def.setLevel(Deflater.BEST_COMPRESSION);
                }
            };
        }

        @Override
        public Boolean call() throws Exception {
            try {
                String marker = (String)options.valueOf("translatePicaSortMarker");
                if ("ntriples".equals(options.valueOf("format"))) {
                    NTripleWriter writer = new NTripleWriter()
                            .output(out)
                            .translatePicaSortMarker(marker);
                    while (true) {
                        String line = pump.take();
                        if ("|".equals(line)) {
                            break;
                        }
                        RdfXmlReader rdfxml = new RdfXmlReader();
                        rdfxml.setTripleListener(writer);
                        rdfxml.parse(new InputSource(new StringReader(line)));
                    }
                    writer.close();
                    docCounter.getAndAdd(writer.getIdentifierCounter());
                    charCounter.getAndAdd(writer.getByteCounter());
                }
                else if ("turtle".equals(options.valueOf("format"))) {
                    TurtleWriter writer = new TurtleWriter()
                            .output(out)
                            .translatePicaSortMarker(marker);
                    while (true) {
                        String line = pump.take();
                        if ("|".equals(line)) {
                            break;
                        }
                        RdfXmlReader rdfxml = new RdfXmlReader();
                        rdfxml.setTripleListener(writer);
                        rdfxml.parse(new InputSource(new StringReader(line)));
                    }
                    writer.close();
                    docCounter.getAndAdd(writer.getIdentifierCounter());
                    charCounter.getAndAdd(writer.getByteCounter());
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

}

