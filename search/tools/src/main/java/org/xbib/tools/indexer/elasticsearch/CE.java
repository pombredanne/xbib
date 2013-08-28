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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elasticsearch.support.bulk.transport.BulkClient;
import org.xbib.elasticsearch.support.bulk.transport.MockBulkClient;
import org.xbib.elasticsearch.support.ingest.transport.IngestClient;
import org.xbib.elasticsearch.support.ingest.transport.MockIngestClient;
import org.xbib.elements.ElementOutput;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.file.Finder;
import org.xbib.io.file.TextFileConnectionFactory;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

public class CE extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(CE.class.getName());

    private static Queue<URI> input;

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final SimpleResourceContext ctx = new SimpleResourceContext();

    private ElementOutput out;

    private boolean done = false;

    public static void main(String[] args) {
        int exitcode = 0;
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.txt");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("bulksize").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
                    accepts("bulks").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("ElasticsearchCEIndexer");
                System.err.println("--elasticsearch <uri>");
                System.err.println("--index <name>");
                System.err.println("--type <name>");
                System.err.println("--path <uri>");
                System.err.println("--pattern <pattern>");
                System.err.println("--threads <num>");
                System.err.println("--bulksize <num>");
                System.err.println("--bulks <num>");
                System.exit(1);
            }

            input = new Finder((String)options.valueOf("pattern"))
                    .find((String)options.valueOf("path"))
                    .getURIs();
            logger.info("found {} input files", input.size());
            final Integer threads = (Integer) options.valueOf("threads");
            URI uri = URI.create(options.valueOf("elasticsearch").toString());
            boolean mock = (Boolean)options.valueOf("mock");

            final BulkClient es = mock ?
                    new MockBulkClient() :
                    new BulkClient();

            es.newClient(uri)
                    .setIndex(options.valueOf("index").toString())
                    .setType(options.valueOf("type").toString())
                    .maxBulkActions((Integer) options.valueOf("bulksize"))
                    .maxConcurrentBulkRequests((Integer) options.valueOf("bulks"));

            logger.info("connected to ES with bulk size {} and max bulks {}",
                    options.valueOf("bulksize"),
                    options.valueOf("bulks"));

            final ElasticsearchResourceSink sink = new ElasticsearchResourceSink(es);

            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new CE(sink);
                        }
                    }).execute();

            logger.info("finished, number of files = {}, resources indexed = {}",
                    fileCounter, sink.getCounter());

            service.shutdown();
            es.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    public CE(ElementOutput out) {
        this.out = out;
    }

    @Override
    public void close() throws IOException {
        // do not clear input
    }

    @Override
    public boolean hasNext() {
        if (input.isEmpty()) {
            done = true;
        }
        return !done && !input.isEmpty();
    }

    @Override
    public AtomicLong next() {
        if (done) {
            return fileCounter;
        }
        try {
            URI uri = input.poll();
            if (uri != null) {
                push(uri);
            } else {
                done = true;
            }
            fileCounter.incrementAndGet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            done = true;
        }
        return fileCounter;
    }
    TextFileConnectionFactory factory = new TextFileConnectionFactory();

    private void push(URI uri) throws Exception {
        if (uri == null) {
            return;
        }
        InputStream in = factory.open(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String title = null;
            String author = null;
            String year = null;
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (title == null && line.startsWith("Titel:")) {
                    title = line.substring("Titel:".length()).trim();
                } else if (author == null && line.startsWith("Autor:")) {
                    author = line.substring("Autor:".length()).trim();
                } else if (year == null && line.startsWith("Jahr:")) {
                    year = line.substring("Jahr:".length()).trim();
                } else if (line.startsWith("ocr-text:")) {
                    sb.append(line.substring("ocr-text:".length()).trim()).append(" ");
                } else {
                    sb.append(line).append(" ");
                }
            }
            String id = uri.getPath();
            if (id.endsWith(".txt")) {
                int pos = id.lastIndexOf("/");
                id = pos >= 0 ? id.substring(pos + 1) : id;
                // remove .txt and force uppercase
                id = id.substring(0, id.length() - 4).toUpperCase();
                IRI identifier = IRI.builder().scheme("urn").host("hbz").query("enrichment").fragment(id).build();
                Resource resource = ctx.newResource();
                resource.id(identifier)
                        .add("dc:title", title)
                        .add("dc:creator", author)
                        .add("dc:date", year)
                        .newResource("dc:description")
                        .add("dcterms:tableOfContents", sb.toString());

                out.output(ctx, ctx.contentBuilder());
            }
        }
    }
}
