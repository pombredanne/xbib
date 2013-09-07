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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.xbib.grouping.bibliographic.endeavor.WorkAuthor;
import org.xbib.importer.AbstractImporter;
import org.xbib.importer.ImportService;
import org.xbib.importer.Importer;
import org.xbib.importer.ImporterFactory;
import org.xbib.io.file.Finder;
import org.xbib.io.file.TextFileConnectionFactory;
import org.xbib.util.URIUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.RDF;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.ResourceSerializer;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.text.InvalidCharacterException;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.util.Entities;
import org.xbib.xml.XMLUtil;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * Convert article DB
 */
public class ArticleDBConverter extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(ArticleDBConverter.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static Charset UTF8 = Charset.forName("UTF-8");

    protected static Queue<URI> input;

    protected final static JsonFactory jsonFactory = new JsonFactory();

    protected final static SimpleResourceContext resourceContext = new SimpleResourceContext();

    private final ResourceSerializer serializer;

    private final ResourceSerializer missingSerializer;

    private final ResourceSerializer errorSerializer;

    private final static TextFileConnectionFactory factory = new TextFileConnectionFactory();

    private boolean done = false;

    private static String outputFilename;

    private static SerialsDBConverter serialsdb;

    private static Map<String,Resource> serials;

    private static FileWriter missingserials;

    private final static AtomicLong dupCounter = new AtomicLong(0L);

    private final static AtomicLong counter = new AtomicLong(0L);

    public static void main(String[] args) {
        int exitcode = 0;
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.json");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("output").withRequiredArg().ofType(String.class).defaultsTo("articles");
                    accepts("serials").withRequiredArg().ofType(String.class).defaultsTo("titleFile.csv");
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + ArticleDBConverter.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.json)" + lf
                        + " --threads <n>          the number of threads (optional, default: <num-of=cpus)"
                        + " --output <path>        a file path from where the output is written (default: articles)" + lf
                        + " --serials <path>        a file path from where the serials are located (default: titleFile.csv)" + lf
                );
                System.exit(1);
            }
            input = new Finder(options.valueOf("serials").toString()).find(options.valueOf("path").toString()).getURIs();

            if (input.isEmpty()) {
                throw new IllegalArgumentException("no serials found: " + options.valueOf("serials") + " in " + options.valueOf("path"));
            }

            logger.info("parsing initial set of serials...");

            for (URI uri : input) {
                InputStream in = factory.open(uri);
                serialsdb = new SerialsDBConverter(new InputStreamReader(in, UTF8), "serials" );
                serials = serialsdb.getMap();
            }
            logger.info("serials done, {}", serials.size());

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();

            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("found {} input files", input);

            IRINamespaceContext context = IRINamespaceContext.newInstance();
            context.addNamespace(RDF.NS_PREFIX, RDF.NS_URI);
            context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
            context.addNamespace("dcterms", "http://purl.org/dc/terms/");
            context.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
            context.addNamespace("frbr", "http://purl.org/vocab/frbr/core#");
            context.addNamespace("fabio", "http://purl.org/spar/fabio/");
            context.addNamespace("prism", "http://prismstandard.org/namespaces/basic/2.1/");
            resourceContext.newNamespaceContext(context);

            outputFilename = (String)options.valueOf("output");

            // for proper resources

            FileOutputStream fout = new FileOutputStream(outputFilename + ".ttl.gz");
            GZIPOutputStream gzout = new GZIPOutputStream(fout){
                {
                    def.setLevel(Deflater.BEST_COMPRESSION);
                }
            };
            final TurtleWriter writer = new TurtleWriter()
                    .setContext(context)
                    .output(gzout);
            writer.writeNamespaces();

            // for erraneous resources (broken encodings)

            FileOutputStream errorfout = new FileOutputStream(outputFilename + "-errors.ttl.gz");
            GZIPOutputStream errorgzout = new GZIPOutputStream(errorfout){
                {
                    def.setLevel(Deflater.BEST_COMPRESSION);
                }
            };
            final TurtleWriter errorWriter = new TurtleWriter()
                    .setContext(context)
                    .output(errorgzout);
            errorWriter.writeNamespaces();

            // for missing serials resources

            FileOutputStream noserialfout = new FileOutputStream(outputFilename + "-without-serial.ttl.gz");
            GZIPOutputStream noserialgzout = new GZIPOutputStream(noserialfout){
                {
                    def.setLevel(Deflater.BEST_COMPRESSION);
                }
            };
            final TurtleWriter noserialWriter = new TurtleWriter()
                    .setContext(context)
                    .output(noserialgzout);
            noserialWriter.writeNamespaces();

            // extra text file for missing serials

            missingserials = new FileWriter("missingserials.txt");

            ImportService service = new ImportService()
                    .threads(threads)
                    .factory(
                            new ImporterFactory() {
                                @Override
                                public Importer newImporter() {
                                    return new ArticleDBConverter(writer, noserialWriter, errorWriter);
                                }
                            }).execute().shutdown();

            logger.info("articles conversion complete: {} counts, {} dups", counter.get(), dupCounter.get());

            service.shutdown();

            gzout.close();
            noserialgzout.close();
            errorgzout.close();

            logger.info("writing serials...");
            serialsdb.writeSerials(new FileWriter("articleserials.txt"));
            logger.info("serials written");

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    public ArticleDBConverter(ResourceSerializer serializer, ResourceSerializer missingSerializer, ResourceSerializer errorSerializer) {
        this.serializer = serializer;
        this.missingSerializer = missingSerializer;
        this.errorSerializer = errorSerializer;
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
            return new AtomicLong(1L);
        }
        try {
            URI uri = input.poll();
            if (uri != null) {
                logger.info("starting process of {}, {} files left, {} counts, {} dups",
                        uri, input.size(), counter.get(), dupCounter.get());
                process(uri);
            } else {
                done = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            done = true;
        }
        return new AtomicLong(1L);
    }

    protected void process(URI uri) throws Exception {
        if (uri == null) {
            return;
        }
        InputStream in = factory.open(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
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
                        switch (result) {
                            case OK:
                                synchronized (serializer) {
                                    serializer.write(resource);
                                }
                                break;
                            case MISSINGSERIAL:
                                synchronized (missingSerializer) {
                                    missingSerializer.write(resource);
                                }
                                break;
                            case ERROR:
                                synchronized (errorSerializer) {
                                    errorSerializer.write(resource);
                                }
                                break;

                        }
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

    protected Result parseCoinsInto(Resource resource, final String value) {
        final IRI coins = IRI.builder()
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
                            doiPart = doiPart.replaceAll("%2F","/"); // case insensitive
                            IRI dereferencable = IRI.builder().scheme("http").host("xbib.info")
                                    .path("/doi/").fragment(doiPart).build();
                            r.id(dereferencable)
                                .a(FABIO_ARTICLE)
                                .add("prism:doi", v.toUpperCase());
                        } catch (Exception e) {
                            logger.warn("can't build IRI from DOI " + v, e);
                        }
                        break;
                    }
                    case "rft.atitle" : {
                        v = Entities.HTML40.unescape(v);
                        r.add("dcterms:title", v);
                        work = v;
                        break;
                    }
                    case "rft.jtitle" : {
                        v = Entities.HTML40.unescape(v);
                        Resource j = r.newResource(FRBR_PARTOF)
                                .a(FABIO_JOURNAL)
                                .add("prism:publicationName", v);
                        if (serials.containsKey(v)) {
                                Resource serial = serials.get(v);
                                Node issn = serial.literal("prism:issn");
                                if (issn != null) {
                                    j.add("prism:issn", issn.toString());
                                }
                                Node publisher = serial.literal("dc:publisher");
                                if (publisher != null) {
                                    j.add("dc:publisher", publisher.toString() );
                                }
                        } else {
                            missingserial = true;
                            synchronized (missingserials) {
                                try {
                                    missingserials.write(v);
                                    missingserials.write("\n");
                                } catch (IOException e) {
                                    logger.error("can't write missing serial info", e);
                                }
                            }
                        }
                        break;
                    }
                    case "rft.aulast" : {
                        v = Entities.HTML40.unescape(v);
                        if (aulast != null) {
                            r.newResource(FOAF_MAKER)
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst);
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
                                    .add("foaf:givenName", aufirst );
                            aulast = null;
                            aufirst = null;
                        } else {
                            aufirst = v;
                        }
                        break;
                    }
                    case "rft.au" : {
                        // fix author
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
                    case "rft.genre" :
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
                            .add("foaf:familyName", aulast)
                            .add("foaf:givenName", aufirst);
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
                    /*if (articles.containsKey(key)) {
                        dupCounter.incrementAndGet();
                    } else {
                        //logger.info("new articles key {}", key);
                        articles.put(key, coins);
                        counter.incrementAndGet();
                    }*/
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
        } catch (InvalidCharacterException | URISyntaxException e) {
            logger.warn("can't parse query string: " + coins, e);
        }
        listener.close();
        return listener.hasErrors() ?
                Result.ERROR : listener.missingSerial() ?
                Result.MISSINGSERIAL : Result.OK;
    }

}
