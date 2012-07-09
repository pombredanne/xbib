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
package org.xbib.tools.medline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.xbib.builders.elasticsearch.ElasticsearchBulkResourceOutput;
import org.xbib.elements.output.ElementOutput;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.InputStreamService;
import org.xbib.io.file.Find;
import org.xbib.io.util.DateUtil;
import org.xbib.rdf.ResourceContext;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.io.XmlHandler;
import org.xbib.rdf.io.XmlReader;
import org.xbib.rdf.io.XmlResourceHandler;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

public final class ElasticsearchMedlineIndexer extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = Logger.getLogger(ElasticsearchMedlineIndexer.class.getName());
    private final static InputStreamService iss = new InputStreamService();
    private final static AtomicLong fileCounter = new AtomicLong(0L);
    private final String INPUT_ENCODING = "UTF-8";
    private boolean done = false;
    private Queue<URI> input;
    private static OptionSet options;
    private final SimpleResourceContext ctx = new SimpleResourceContext();
    private ElementOutput out;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {

                {
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("es").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("medline*.xml.gz");
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for ElasticsearchMedlineIndexer");
                System.err.println(" --help                 print this help message");
                System.err.println(" --es <uri>             Elasticesearch URI");
                System.err.println(" --index <index>        Elasticsearch index name");
                System.err.println(" --type <type>          Elasticsearch type name");
                System.err.println(" --path <path>          a file path from where the input files are recursively collected (required)");
                System.err.println(" --pattern <pattern>    a regex for selecting matching file names for input (required)");
                System.err.println(" --threads <n>          the number of threads (required, default: 1)");
                System.exit(1);
            }
            final Queue<URI> input = new Find(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.log(Level.INFO, "input = {0},  threads = {1}", new Object[]{input, threads});

            final ElasticsearchBulkResourceOutput es = new ElasticsearchBulkResourceOutput<SimpleResourceContext>();
            es.connect(URI.create(options.valueOf("es").toString()), options.valueOf("index").toString(), options.valueOf("type").toString());

            ImportService service = new ImportService().setThreads(threads).setFactory(
                    new ImporterFactory() {

                        @Override
                        public Importer newImporter() {
                            return new ElasticsearchMedlineIndexer(es).setInput(input);
                        }
                    }).run(input);
            service.waitFor();

            logger.log(Level.INFO, "files = {0}, docs indexed = {1}", new Object[]{fileCounter, es.getCounter()});

            es.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.err.println("Sucess!");
        System.exit(0);
    }

    public ElasticsearchMedlineIndexer(ElementOutput out) {
        this.out = out;
    }

    public ElasticsearchMedlineIndexer setInput(Queue<URI> list) {
        if (list != null) {
            this.input = list;
        }
        return this;
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
        URI uri = input.isEmpty() ? null : input.poll();
        done = (uri == null);
        if (done) {
            return fileCounter;
        }
        BufferedReader br = null;
        try {
            InputStream in = iss.getInputStream(uri);
            br = new BufferedReader(new InputStreamReader(in, INPUT_ENCODING));
            XmlHandler handler = new Handler(ctx).setListener(new ResourceBuilder()).setDefaultNamespace("ml", "http://www.nlm.nih.gov/medline");
            new XmlReader().setHandler(handler).setNamespaces(false).parse(br);
            fileCounter.incrementAndGet();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return fileCounter;
    }

    class Handler extends XmlResourceHandler {

        public Handler(ResourceContext context) {
            super(context);
        }

        @Override
        public void closeResource() {
            super.closeResource();
            out.output(ctx, DateUtil.formatNow());
        }

        @Override
        public boolean isResourceDelimiter(QName name) {
            return "MedlineCitation".equals(name.getLocalPart());
        }

        @Override
        public URI identify(QName name, String value, URI identifier) {
            if ("PMID".equals(name.getLocalPart())) {
                try {
                    URI u = URI.create("pmid:" + value);
                    return u;
                } catch (Exception e) {
                }
            }
            return null;
        }

        @Override
        public boolean skip(QName name) {
            boolean skipped = "MedlineCitationSet".equals(name.getLocalPart())
                    || "MedlineCitation".equals(name.getLocalPart());
            return skipped;
        }
    }

    class ResourceBuilder implements StatementListener {

        @Override
        public void newIdentifier(URI identifier) {
            ctx.resource().setIdentifier(identifier);
        }

        @Override
        public void statement(Statement statement) {
            ctx.resource().add(statement);
        }
    }

}
