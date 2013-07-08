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
import org.xbib.io.util.URIUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.ResourceSerializer;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.text.InvalidCharacterException;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * Convert article DB
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ArticleDBConverter extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(ArticleDBConverter.class.getName());

    private final static String lf = System.getProperty("line.separator");

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

    private static Map<String,IRI> articles;

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
                InputStream in = factory.getInputStream(uri);
                serialsdb = new SerialsDBConverter(new InputStreamReader(in, "UTF-8"), "serials" );
                serials = serialsdb.getMap();
            }
            logger.info("serials done, {}", serials.size());

            articles = new ConcurrentHashMap();

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();

            final Integer threads = (Integer) options.valueOf("threads");

            logger.info("found {} input files", input);

            IRINamespaceContext context = IRINamespaceContext.newInstance();
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
                logger.info("starting process of {}, {} files left", uri, input.size());
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
        InputStream in = factory.getInputStream(uri);
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

    protected enum Result {
        OK, ERROR, MISSINGSERIAL
    }

    protected Result parseCoinsInto(Resource resource, final String value) {
        final IRI coins = IRI.builder()
                .scheme("http")
                .host("localhost")
                .query(XMLUtil.unescape(value)).build();
        resource.add("rdf:type", FABIO_ARTICLE );
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
                        try {
                            // info URI RFC wants slash as unencoded character
                            String doiPart = URIUtil.encode(v, Charset.forName("UTF-8"));
                            doiPart = doiPart.replaceAll("%2F","/");
                            IRI doi = IRI.builder().curi("info", "doi/" + doiPart).build();
                            IRI id = IRI.builder().scheme("http").host("xbib.info")
                                    .path("/works/doi").fragment(doiPart).build();
                            r.id(id);
                            r.add("dcterms:identifier", doi)
                                    .add("prism:doi", v);
                        } catch (Exception e) {
                            logger.warn("can't build IRI from DOI " + v, e);
                        }
                        break;
                    }
                    case "rft.atitle" : {
                        r.add("dcterms:title", v);
                        work = v;
                        break;
                    }
                    case "rft.jtitle" : {
                        Resource j = r.newResource("frbr:partOf")
                                .add("rdf:type", FABIO_JOURNAL)
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
                        if (aulast != null) {
                            r.newResource("foaf:maker")
                                    .add("foaf:familyName", aulast)
                                    .add("foaf:givenName", aufirst);
                            aulast = null;
                            aufirst = null;
                        } else {
                            aulast = v;
                        }
                        if (author == null) {
                            author = v;
                        }
                        break;
                    }
                    case "rft.aufirst" : {
                        if (aufirst != null) {
                            r.newResource("foaf:maker")
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
                        r.add("dc:creator", v);
                        author = v;
                        break;
                    }
                    case "rft.date" : {
                        Literal l = new SimpleLiteral<>(v).type(Literal.GYEAR);
                        r.add("prism:publicationDate", l);
                        break;
                    }
                    case "rft.volume" : {
                        r.newResource("frbr:embodiment")
                                .add("rdf:type", FABIO_PERIODICAL_VOLUME)
                                .add("prism:volume", v);
                        break;
                    }
                    case "rft.issue" : {
                        r.newResource("frbr:embodiment")
                                .add("rdf:type", FABIO_PERIODICAL_ISSUE )
                                .add("prism:issueIdentifier", v);
                        break;
                    }
                    case "rft.spage" : {
                        if (spage != null) {
                            r.newResource("frbr:embodiment")
                                    .add("rdf:type", FABIO_PRINT_OBJECT)
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
                            r.newResource("frbr:embodiment")
                                    .add("rdf:type", FABIO_PRINT_OBJECT)
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
                    r.newResource("foaf:maker")
                            .add("foaf:familyName", aulast)
                            .add("foaf:givenName", aufirst);
                }
                if (spage != null || epage != null) {
                    r.newResource("frbr:embodiment")
                            .add("rdf:type", FABIO_PRINT_OBJECT)
                            .add("prism:startingPage", spage)
                            .add("prism:endingPage", epage);
                }
                // create bibliographic key
                String key = new WorkAuthor()
                        .authorName(author)
                        .workName(work)
                        .createIdentifier();
                if (author!= null && work != null && key != null) {
                    r.add("dc:identifier", key);
                    if (articles.containsKey(key)) {
                        //logger.warn("duplicate article key '{}' new==> {} old==> {}", key, coins, articles.get(key));
                        dupCounter.incrementAndGet();
                    } else {
                        //logger.info("new articles key {}", key);
                        articles.put(key, coins);
                        counter.incrementAndGet();
                    }
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
            URIUtil.parseQueryString(coins.toURI(), Charset.forName("UTF-8"), listener);
        } catch (InvalidCharacterException | URISyntaxException e) {
            logger.warn("can't parse query string: " + coins, e);
        }
        listener.close();
        return listener.hasErrors() ? Result.ERROR : listener.missingSerial() ? Result.MISSINGSERIAL : Result.OK;
    }

}
