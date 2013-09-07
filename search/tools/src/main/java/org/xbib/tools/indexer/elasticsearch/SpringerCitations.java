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

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elasticsearch.support.bulk.transport.BulkClient;
import org.xbib.elasticsearch.support.bulk.transport.MockBulkClient;
import org.xbib.elements.ElementOutput;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.file.Finder;
import org.xbib.io.file.TextFileConnectionFactory;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Push Springer citations to Elasticsearch
 *
 */
public class SpringerCitations extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(SpringerCitations.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private static Queue<URI> input;

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final SimpleResourceContext resourceContext = new SimpleResourceContext();

    private static String index;

    private static String type;

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
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(4 * Runtime.getRuntime().availableProcessors());
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.txt");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + Medline.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 1000)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 4 * cpu cores)"
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.txt)" + lf
                        + " --threads <n>          the number of threads (optional, default: <num-of=cpus)"
                );
                System.exit(1);
            }
            input = new Finder((String)options.valueOf("pattern"))
                    .find((String) options.valueOf("path"))
                    .getURIs();
            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("found {} input files", input.size());

            URI esURI = URI.create((String)options.valueOf("elasticsearch"));
            index = (String)options.valueOf("index");
            type = (String)options.valueOf("type");
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            boolean mock = (Boolean)options.valueOf("mock");

            final BulkClient es = mock ?
                    new MockBulkClient() :
                    new BulkClient();

            es.maxBulkActions(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .newClient(esURI)
                    .waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));

            logger.info("creating new index ...");
            es.setIndex(index)
                    .setType(type)
                    .newIndex(true); // true = ignore IndexAlreadyExistsException
            logger.info("... new index created");

            final ElasticsearchResourceSink sink = new ElasticsearchResourceSink(es);

            ImportService service = new ImportService().threads(threads).factory(
                    new ImporterFactory() {
                        @Override
                        public Importer newImporter() {
                            return new SpringerCitations(sink);
                        }
                    }).execute();

            logger.info("finished, number of files = {}, resources indexed = {}",
                    fileCounter, sink.getCounter());

            service.shutdown();
            logger.info("service shutdown");

            es.shutdown();
            logger.info("elasticsearch client shutdown");

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    public SpringerCitations(ElementOutput out) {
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
        return !done;
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

    private IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    private IRI FABIO_JOURNAL = IRI.create("fabio:Journal");

    private IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");

    private IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");

    private IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");

    private IRI FRBR_PARTOF = IRI.create("frbr:partOf");

    private IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");

    private final static TextFileConnectionFactory factory = new TextFileConnectionFactory();

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
            List<String> author = new LinkedList();
            String year = null;
            String journal = null;
            String issn = null;
            String volume = null;
            String issue = null;
            String pagination = null;
            String doi = null;
            String publisher = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if ('%' != line.charAt(0)) {
                    continue;
                }
                char ch = line.charAt(1);
                switch (ch) {
                    case 'D' : {
                        year = line.substring(3).trim();
                        break;
                    }
                    case 'T' : {
                        title = line.substring(3).trim();
                        break;
                    }
                    case '@' : {
                        issn = line.substring(3).trim();
                        break;
                    }
                    case 'J' : {
                        journal = line.substring(3).trim();
                        break;
                    }
                    case 'A' : {
                        author.add(line.substring(3).trim());
                        break;
                    }
                    case 'V' : {
                        volume = line.substring(3).trim();
                        break;
                    }
                    case 'N' : {
                        issue = line.substring(3).trim();
                        break;
                    }
                    case 'P' : {
                        pagination = line.substring(3).trim();
                        break;
                    }
                    case 'R' : {
                        doi = line.substring(3).trim();
                        break;
                    }
                    case 'I' : {
                        publisher = line.substring(3).trim();
                        break;
                    }
                    case 'U' : {
                        // URL (DOI resolver)
                        break;
                    }
                    case 'K' : {
                        // keywords
                        break;
                    }
                    case '0' : {
                        // record type
                        break;
                    }
                    case '8' : {
                        // day
                        break;
                    }
                    case 'G' : {
                        // language
                        break;
                    }
                    default: {
                        logger.warn("unknown tag: " + line);
                    }
                }
            }
            // create bibliographic key

            String key = author.isEmpty() ? null : new WorkAuthor()
                    .authorName(author.get(0))
                    .workName(title)
                    .createIdentifier();

            IRI dereferencable = IRI.builder().scheme("http").host("xbib.info")
                    .path("/doi/").fragment(doi).build();

            Resource r = resourceContext.newResource()
                    .id(dereferencable)
                    .a(FABIO_ARTICLE)
                    .add("xbib:key", key)
                    .add("prism:doi", doi)
                    .add("dc:title", title);
            for (String a : author) {
                r.add("dc:creator", a);
            }
            r.add("prism:publicationDate", new SimpleLiteral<>(year).type(Literal.GYEAR));
            r.newResource(FRBR_EMBODIMENT)
                    .a(FABIO_PERIODICAL_VOLUME)
                    .add("prism:volume", volume);
            r.newResource(FRBR_EMBODIMENT)
                    .a(FABIO_PERIODICAL_ISSUE)
                    .add("prism:number", issue);
            r.newResource(FRBR_EMBODIMENT)
                    .a(FABIO_PRINT_OBJECT)
                    .add("prism:pageRange", pagination);
            r.newResource(FRBR_PARTOF)
                    .a(FABIO_JOURNAL)
                    .add("prism:publicationName", journal)
                    .add("prism:issn", issn)
                    .add("dc:publisher", publisher);
            resourceContext.resource().id(IRI.builder()
                    .scheme("http")
                    .host(index)
                    .query(type)
                    .fragment(resourceContext.resource().id().getFragment())
                    .build());
            out.output(resourceContext, resourceContext.contentBuilder());
        }
    }
}
