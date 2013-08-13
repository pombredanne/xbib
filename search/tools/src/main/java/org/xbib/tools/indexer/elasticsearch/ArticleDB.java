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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;

import org.xbib.elasticsearch.ElasticsearchResourceSink;
import org.xbib.elasticsearch.support.ingest.transport.IngestClient;
import org.xbib.elasticsearch.support.ingest.transport.MockIngestClient;
import org.xbib.elasticsearch.support.search.transport.SearchClientSupport;
import org.xbib.elements.ElementOutput;
import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.file.Finder;
import org.xbib.io.file.TextFileConnectionFactory;
import org.xbib.io.util.URIUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RDF;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.text.InvalidCharacterException;
import org.xbib.tools.convert.SerialsDBConverter;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.FormatUtil;
import org.xbib.util.Entities;
import org.xbib.xml.XMLUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Index article DB into Elasticsearch
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ArticleDB extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(ArticleDB.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static JsonFactory jsonFactory = new JsonFactory();

    private final static AtomicLong resourceCounter = new AtomicLong(0L);

    private final static AtomicLong inputCounter = new AtomicLong(0L);

    private final static TextFileConnectionFactory factory = new TextFileConnectionFactory();

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private static SerialsDBConverter serialsdb;

    private static Map<String,Resource> serials;

    private static Queue<URI> input;

    private static String index;

    private static String type;

    private final ElementOutput output;

    private boolean done = false;

    public static void main(String[] args) {
        int exitcode = 0;
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.json");
                    accepts("serials").withRequiredArg().ofType(String.class).required().defaultsTo("titleFile.csv");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + ArticleDB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.json)" + lf
                        + " --serials <path>        a file path from where the serials are located (default: titleFile.csv)" + lf
                        + " --threads <n>          the number of threads (optional, default: <num-of=cpus)"
                );
                System.exit(1);
            }
            input = new Finder(options.valueOf("serials").toString()).find(options.valueOf("path").toString()).getURIs();

            logger.info("parsing initial set of serials...");

            for (URI uri : input) {
                InputStream in = factory.open(uri);
                serialsdb = new SerialsDBConverter(new InputStreamReader(in, UTF8), "serials" );
                serials = serialsdb.getMap();
            }
            logger.info("serials done, size = {}", serials.size());

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();

            logger.info("found {} input files", input.size());

            URI esURI = URI.create((String)options.valueOf("elasticsearch"));
            index = (String)options.valueOf("index");
            type = (String)options.valueOf("type");
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            boolean mock = (Boolean)options.valueOf("mock");

            final IngestClient es = mock ?
                    new MockIngestClient() :
                    new IngestClient();

            es.maxBulkActions(maxbulkactions)
                    .maxConcurrentBulkRequests(maxconcurrentbulkrequests)
                    .newClient(esURI)
                    .waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));

            final SearchClientSupport search = new SearchClientSupport()
                    .newClient(esURI);

            logger.info("creating new index ...");
            es.setIndex(index)
                    .setType(type)
                    .newIndex(true); // true = ignore IndexAlreadyExistsException
            logger.info("... new index created");

            final ElasticsearchResourceSink<ResourceContext, Resource> sink =
                    new ElasticsearchResourceSink(es);

            long t0 = System.currentTimeMillis();
            ImportService service = new ImportService()
                    .threads( (Integer) options.valueOf("threads") )
                    .factory(
                            new ImporterFactory() {
                                @Override
                                public Importer newImporter() {
                                    return new ArticleDB(sink);
                                }
                            }).execute().shutdown();

            long t1 = System.currentTimeMillis();
            long docs = resourceCounter.get();
            long bytes = es.getVolumeInBytes();
            double dps = docs * 1000.0 / (double)(t1 - t0);
            double avg = bytes / (docs + 1.0); // avoid div by zero
            double mbps = (bytes * 1000.0 / (double)(t1 - t0)) / (1024.0 * 1024.0) ;
            NumberFormat formatter = NumberFormat.getNumberInstance();
            logger.info("Indexing complete. {} input files, {} docs, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
                    inputCounter,
                    docs,
                    TimeValue.timeValueMillis(t1 - t0).format(),
                    (t1-t0),
                    FormatUtil.convertFileSize(bytes),
                    bytes,
                    FormatUtil.convertFileSize(avg),
                    formatter.format(avg),
                    formatter.format(dps),
                    formatter.format(mbps));

            es.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    private ArticleDB(ElementOutput output) {
        this.output = output;
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
            return inputCounter;
        }
        try {
            while (!done) {
                URI uri = input.poll();
                if (uri != null) {
                    process(uri);
                } else {
                    done = true;
                }
                inputCounter.incrementAndGet();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            done = true;
        }
        return inputCounter;
    }

    protected void process(URI uri) throws Exception {
        if (uri == null) {
            return;
        }
        InputStream in = factory.open(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }

        final IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace(RDF.NS_PREFIX, RDF.NS_URI);
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("dcterms", "http://purl.org/dc/terms/");
        context.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
        context.addNamespace("frbr", "http://purl.org/vocab/frbr/core#");
        context.addNamespace("fabio", "http://purl.org/spar/fabio/");
        context.addNamespace("prism", "http://prismstandard.org/namespaces/basic/2.1/");

        final SimpleResourceContext resourceContext = new SimpleResourceContext();
        resourceContext.newNamespaceContext(context);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, UTF8))) {
            JsonParser parser = jsonFactory.createParser(reader);
            JsonToken token = parser.nextToken();
            Resource resource = null;
            String key = null;
            String value;
            Result result = Result.OK;
            while (token != null) {
                switch (token) {
                    case START_OBJECT: {
                        resource = resourceContext.newResource();
                        break;
                    }
                    case END_OBJECT: {
                        String indexType = type;
                        switch (result) {
                            case OK:
                                indexType = type;
                                break;
                            case MISSINGSERIAL:
                                indexType = type + "noserials";
                                break;
                            case ERROR:
                                indexType = type + "errors";
                                break;
                        }
                        resourceContext.resource().id(IRI.builder()
                                .scheme("http")
                                .host(index)
                                .query(indexType)
                                .fragment(resourceContext.resource().id().getFragment())
                                .build());
                        output.output(resourceContext, resourceContext.contentBuilder());
                        resourceCounter.incrementAndGet();
                        resource = null;
                        break;
                    }
                    case START_ARRAY: {
                        break;
                    }
                    case END_ARRAY: {
                        break;
                    }
                    case FIELD_NAME: {
                        key = parser.getCurrentName();
                        break;
                    }
                    case VALUE_STRING:
                    case VALUE_NUMBER_INT:
                    case VALUE_NUMBER_FLOAT:
                    case VALUE_NULL:
                    case VALUE_TRUE:
                    case VALUE_FALSE: {
                        value = parser.getText();
                        if ("coins".equals(key)) {
                            result = parseCoinsInto(resource, value);
                        }
                        break;
                    }
                    default:
                        throw new IOException("unknown token: " + token);
                }
                token = parser.nextToken();
            }
        }
    }

    protected interface URIListener extends URIUtil.ParameterListener {

        void close();

        boolean hasErrors();

        boolean missingSerial();
    }

    private IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    private IRI FABIO_JOURNAL = IRI.create("fabio:Journal");

    private IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");

    private IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");

    private IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");

    private IRI FOAF_MAKER = IRI.create("foaf:maker");

    private IRI FOAF_AGENT = IRI.create("foaf:agent");

    private IRI FRBR_PARTOF = IRI.create("frbr:partOf");

    private IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");

    protected enum Result {
        OK, ERROR, MISSINGSERIAL
    }

    protected Result parseCoinsInto(Resource resource, String value) {
        IRI coins = IRI.builder()
                .scheme("http")
                .host("localhost")
                .query(XMLUtil.unescape(value)).build();
        final Resource r = resource;
        URIListener listener = new URIListener() {
            boolean error = false;
            boolean missingserial = false;

            String aufirst = null;
            String aulast = null;

            String spage = null;
            String epage = null;

            Resource j = null;
            String title = null;
            Collection<Node> issns = null;
            String year = null;

            String author = null;
            String work = null;

            @Override
            public void received(String k, String v) {
                if (v == null) {
                    return;
                }
                v = v.trim();
                if (v.isEmpty()) {
                    return;
                }
                if (v.indexOf('\uFFFD') >= 0) { // Unicode replacement character
                    error = true;
                }
                switch (k) {
                    case "rft_id" : {
                        if (v.startsWith("info:doi/")) {
                            v = v.substring(9);
                        }
                        // DOI is case-insensitive
                        v = v.toUpperCase();
                        try {
                            // info URI RFC wants slash as unencoded character
                            String doiPart = URIUtil.encode(v, UTF8);
                            doiPart = doiPart.replaceAll("%2F","/");
                            IRI dereferencable = IRI.builder().scheme("http").host("xbib.info")
                                    .path("/doi/").fragment(doiPart).build();
                            r.id(dereferencable)
                                .a(FABIO_ARTICLE)
                                .add("prism:doi", v);
                        } catch (Exception e) {
                            logger.warn("can't build IRI from DOI " + v, e);
                        }
                        break;
                    }
                    case "rft.atitle" : {
                        v = Entities.HTML40.unescape(v);
                        r.add("dc:title", v);
                        work = v;
                        break;
                    }
                    case "rft.jtitle" : {
                        v = Entities.HTML40.unescape(v);
                        title = v;
                        j = r.newResource(FRBR_PARTOF)
                                .a(FABIO_JOURNAL)
                                .add("prism:publicationName", v);
                        if (serials.containsKey(v)) {
                            Resource serial = serials.get(v);
                            issns = serial.objects("prism:issn");
                            if (issns != null) {
                                Iterator<Node> it = issns.iterator();
                                while (it.hasNext()) {
                                    j.add("prism:issn", it.next().toString());
                                }
                            }
                            Node publisher = serial.literal("dc:publisher");
                            if (publisher != null) {
                                j.add("dc:publisher", publisher.toString() );
                            }
                        } else {
                            missingserial = true;
                        }
                        break;
                    }
                    case "rft.aulast" : {
                        v = Entities.HTML40.unescape(v);
                        if (aulast != null) {
                            r.newResource(FOAF_MAKER)
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst );
                            author = aulast + " " + aufirst;
                            aulast = null;
                            aufirst = null;
                        } else {
                            aulast = v;
                        }
                        break;
                    }
                    case "rft.aufirst" : {
                        v = Entities.HTML40.unescape(v);
                        if (aufirst != null) {
                            r.newResource(FOAF_MAKER)
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst);
                            author = aulast + " " + aufirst;
                            aulast = null;
                            aufirst = null;
                        } else {
                            aufirst = v;
                        }
                        break;
                    }
                    case "rft.au" : {
                        // fix author strings
                        if ("&NA;".equals(v)) {
                            v = null;
                        } else {
                            v = Entities.HTML40.unescape(v);
                        }
                        r.add("dc:creator", v);
                        if (author == null) {
                            author = v;
                        }
                        break;
                    }
                    case "rft.date" : {
                        year = v;
                        Literal l = new SimpleLiteral(v).type(Literal.GYEAR);
                        r.add("prism:publicationDate", l);
                        break;
                    }
                    case "rft.volume" : {
                        r.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PERIODICAL_VOLUME)
                                .add("prism:volume", v);
                        break;
                    }
                    case "rft.issue" : {
                        r.newResource(FRBR_EMBODIMENT)
                                .a(FABIO_PERIODICAL_ISSUE)
                                .add("prism:number", v);
                        break;
                    }
                    case "rft.spage" : {
                        if (spage != null) {
                            r.newResource(FRBR_EMBODIMENT)
                                    .a(FABIO_PRINT_OBJECT)
                                    .add("prism:startingPage", spage)
                                    .add("prism:endingPage", epage);
                            spage = null;
                            epage = null;
                        } else {
                            spage = v;
                        }
                        break;
                    }
                    case "rft.epage" : {
                        if (epage != null) {
                            r.newResource(FRBR_EMBODIMENT)
                                    .a(FABIO_PRINT_OBJECT)
                                    .add("prism:startingPage", spage)
                                    .add("prism:endingPage", epage);
                            spage = null;
                            epage = null;
                        } else {
                            epage = v;
                        }
                        break;
                    }
                    case "rft_val_fmt":
                    case "rft.genre":
                    case "ctx_ver":
                    case "rfr_id":
                        break;
                    default: {
                        logger.info("unknown element: {}", k);
                        break;
                    }
                }
            }

            public void close() {
                // pending fields...
                if (aufirst != null || aulast != null) {
                    r.newResource(FOAF_MAKER)
                            .a(FOAF_AGENT)
                            .add("foaf:familyName", aulast)
                            .add("foaf:givenName", aufirst);
                    author = aulast + " " + aufirst;
                }
                if (spage != null || epage != null) {
                    r.newResource(FRBR_EMBODIMENT)
                            .a(FABIO_PRINT_OBJECT)
                            .add("prism:startingPage", spage)
                            .add("prism:endingPage", epage);
                }
                // create bibliographic key
                String key = new WorkAuthor()
                        .authorName(author)
                        .workName(work)
                        .createIdentifier();
                // move aulast to author if no author
                if (author == null && aulast != null) {
                    author = aulast;
                }
                if (author!= null && work != null && key != null) {
                    r.add("xbib:key", key);
                }
            }

            public boolean hasErrors() {
                return error;
            }

            public boolean missingSerial() {
                return missingserial;
            }
        };
        try {
            URIUtil.parseQueryString(coins.toURI(), UTF8, listener);
        } catch (InvalidCharacterException | URISyntaxException  e) {
            logger.warn("can't parse query string: " + coins, e);
        }
        listener.close();
        return listener.hasErrors() ?
                Result.ERROR : listener.missingSerial() ?
                Result.MISSINGSERIAL : Result.OK;
    }

}
