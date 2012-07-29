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
package org.xbib.tools.aleph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.BytesProgressWatcher;
import org.xbib.io.InputStreamService;
import org.xbib.io.ProgressMonitoredOutputStream;
import org.xbib.io.SplitWriter;
import org.xbib.io.file.Finder;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.DefaultMarcXchangeListener;
import org.xbib.marc.FieldDesignator;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.addons.AlephSequentialReader;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class AlephSeq2MarcXML extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(AlephSeq2MarcXML.class.getName());
    private final static AtomicLong fileCounter = new AtomicLong(0L);
    private final int BUFFER_SIZE = 8192;
    private final String INPUT_ENCODING = "UTF-8";
    private final String OUTPUT_ENCODING = "UTF-8";
    private final BytesProgressWatcher watcher;
    private boolean done = false;
    private static String output;
    private static String basename;
    private long splitsize = 1000000L;
    private String linkformat = "http://index.hbz-nrw.de/query/services/document/xhtml/hbz/title/%s";
    private Queue<URI> input;
    private List<Integer> enable;

    public static void main(String[] args) {

        try {
            OptionParser parser = new OptionParser() {

                {
                    accepts("output").withRequiredArg().ofType(String.class).required();
                    accepts("basename").withRequiredArg().ofType(String.class).required();
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required();
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("enable").withRequiredArg().ofType(Integer.class);
                    accepts("linkformat").withRequiredArg().ofType(String.class);
                    accepts("splitsize").withRequiredArg().ofType(Long.class);
                    accepts("counter").withRequiredArg().ofType(Long.class).defaultsTo(0L);
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for AlephSeq2MarcXML");
                System.err.println(" --help                 print this help message");
                System.err.println(" --output <dir>         the output directory for generated MARC XML, will be created if it does not exist");
                System.err.println(" --basename <name>      the base name for generated MARC XML files, will be appended with '_' <counter> '.xml' (required)");
                System.err.println(" --counter <n>          the start counter for the base name MARC XML files (default: 0)");
                System.err.println(" --path <path>          a file path from where the input files are recursively collected (required)");
                System.err.println(" --pattern <pattern>    a regex for selecting mathing file names for input (required)");
                System.err.println(" --enable [941|956]     enables MARC field 941 or 956 (default:not enabled)");
                System.err.println(" --linkformat <format>  format string for generating MARC 956 field. Example \"http://index.hbz-nrw.de/query/services/document/xhtml/hbz/title/%s\"  ");
                System.err.println(" --splitsize <n>        the maximum size of a MARC XML file piece (in octets, default: 1000000)");
                System.err.println(" --threads <n>          the number of threads  (required, default: 1)");
                System.exit(1);
            }
            output = (String) options.valueOf("output");
            basename = (String) options.valueOf("basename");
            final Queue<URI> input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            final List<Integer> enable = (List<Integer>) options.valuesOf("enable");
            final String linkformat = (String) options.valueOf("linkformat");
            final Long splitsize = (Long) options.valueOf("splitsize");
            fileCounter.set((Long) options.valueOf("counter"));
            final Integer threads = (Integer) options.valueOf("threads");

            System.err.println("AlephSeq2MarcXML running with the following parameters");
            System.err.println("input = " + input);
            System.err.println("output = " + output);
            System.err.println("basename = " + basename);
            System.err.println("counter = " + fileCounter.get());
            System.err.println("threads = " + threads + " (default:1)");
            System.err.println("split size = " + splitsize + " (default:1000000)");
            System.err.println("enable = " + enable + " ");
            System.err.println("linkformat (for MARC 956) = " + linkformat + " (default:\"http://index.hbz-nrw.de/query/services/document/xhtml/hbz/title/%s\")");

            ImportService service = new ImportService().setThreads(threads).setFactory(
                    new ImporterFactory() {

                        @Override
                        public Importer newImporter() {
                            AlephSeq2MarcXML importer = new AlephSeq2MarcXML().setInput(input).setEnable(enable).setLinkPattern(linkformat).setSplitSize(splitsize);
                            return importer;
                        }
                    }).execute(input);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public AlephSeq2MarcXML() {
        this.watcher = new BytesProgressWatcher(BUFFER_SIZE);
    }

    @Override
    public AlephSeq2MarcXML setURI(URI uri) {
        return this;
    }

    public AlephSeq2MarcXML setInput(Queue<URI> list) {
        if (list != null) {
            this.input = list;
        }
        return this;
    }

    public AlephSeq2MarcXML setSplitSize(Long size) {
        if (size != null) {
            this.splitsize = size;
        }
        return this;
    }

    public AlephSeq2MarcXML setLinkPattern(String linkpattern) {
        if (linkpattern != null) {
            this.linkformat = linkpattern;
        }
        return this;
    }

    public AlephSeq2MarcXML setEnable(List<Integer> enable) {
        if (enable != null) {
            this.enable = enable;
        }
        return this;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean hasNext() {
        return !done && !input.isEmpty();
    }

    @Override
    public AtomicLong next() {
        URI uri = input.isEmpty() ? null : input.poll();
        done = uri == null;
        if (done) {
            return fileCounter;
        }
        BufferedReader br = null;
        try {
            InputStream in = InputStreamService.getInputStream(uri);
            br = new BufferedReader(new InputStreamReader(in, INPUT_ENCODING));
            AlephSequentialReader seq = new AlephSequentialReader(br);
            try (SplitWriter bw = new SplitWriter(newWriter(watcher), BUFFER_SIZE)) {
                final Iso2709Reader reader = new Iso2709Reader();
                final StreamResult target = new StreamResult(bw);
                reader.setMarcXchangeListener(new DefaultMarcXchangeListener() {

                    @Override
                    public void endRecord() {
                        if (enable != null) {
                            for (Integer n : enable) {
                                if (n == 941) {
                                    FieldDesignator f1 = new FieldDesignator("941", "  ");
                                    f1.setSubfieldId("d");
                                    reader.getAdapter().beginDataField(f1);
                                    reader.getAdapter().beginSubField(f1);
                                    f1.setData(" 1");
                                    reader.getAdapter().endSubField(f1);
                                    reader.getAdapter().endDataField(null);
                                } else if (n == 956) {
                                    if (linkformat != null) {
                                        FieldDesignator f2 = new FieldDesignator("956", "  ");
                                        f2.setSubfieldId("u");
                                        reader.getAdapter().beginDataField(f2);
                                        reader.getAdapter().beginSubField(f2);
                                        String p = String.format(linkformat, reader.getAdapter().getIdentifier());
                                        f2.setData(" " + p);
                                        reader.getAdapter().endSubField(f2);
                                        reader.getAdapter().endDataField(null);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void trailer(String trailer) {
                        if (watcher.getBytesTransferred() > splitsize) {
                            try {
                                reader.getAdapter().endCollection();
                                target.getWriter().flush();
                                bw.split(newWriter(watcher));
                                reader.getAdapter().beginCollection();
                                watcher.resetWatcher();
                            } catch (IOException | SAXException ex) {
                                logger.error(ex.getMessage(), ex);
                            }
                        }
                    }
                });
                reader.setProperty(Iso2709Reader.SCHEMA, "marc21");
                reader.setProperty(Iso2709Reader.FORMAT, "Marc21");
                reader.setProperty(Iso2709Reader.TYPE, "Bibliographic");
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                transformer.transform(new SAXSource(reader, new InputSource(seq)), target);
            }
        } catch (IOException | SAXNotRecognizedException | SAXNotSupportedException | TransformerFactoryConfigurationError | TransformerException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return fileCounter;
    }

    private Writer newWriter(BytesProgressWatcher watcher) throws IOException {
        File dir = new File(output);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.isDirectory() && !dir.canWrite()) {
            throw new IOException("unable to write to directory " + output);
        }
        String filename = dir + File.separator + basename + "_" + fileCounter.getAndIncrement() + ".xml";
        OutputStream out = new ProgressMonitoredOutputStream(new FileOutputStream(filename), watcher);
        return new OutputStreamWriter(out, OUTPUT_ENCODING);
    }
}
