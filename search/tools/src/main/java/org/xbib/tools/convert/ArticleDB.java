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
import org.xbib.elements.output.ElementOutput;
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
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.RDFSerializer;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.xml.XMLUtil;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Convert article DB export
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ArticleDB extends AbstractImporter<Long, AtomicLong> {

    private final static Logger logger = LoggerFactory.getLogger(ArticleDB.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private static Queue<URI> input;

    private final static AtomicLong fileCounter = new AtomicLong(0L);

    private final static AtomicLong resourceCounter = new AtomicLong(0L);

    private final static SimpleResourceContext resourceContext = new SimpleResourceContext();

    private final static JsonFactory jsonFactory = new JsonFactory();

    private final RDFSerializer serializer;

    private final TextFileConnectionFactory factory = new TextFileConnectionFactory();

    private boolean done = false;


    public static void main(String[] args) {
        int exitcode = 0;
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("*.json");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());

                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + ArticleDB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.json)" + lf
                        + " --threads <n>          the number of threads (optional, default: <num-of=cpus)"
                );
                System.exit(1);
            }

            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("path").toString()).getURIs();

            final Integer threads = (Integer) options.valueOf("threads");
            boolean mock = (Boolean)options.valueOf("mock");

            logger.info("found {} input files. Mock = {}", input.size(), mock);

            IRINamespaceContext context = IRINamespaceContext.newInstance();
            context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
            context.addNamespace("dcterms", "http://purl.org/dc/terms/");
            context.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
            context.addNamespace("frbr", "http://purl.org/vocab/frbr/core#");
            context.addNamespace("fabio", "http://purl.org/spar/fabio/");
            context.addNamespace("prism", "http://prismstandard.org/namespaces/basic/2.0/");
            context.addNamespace("bf", "http://bibframe.org/vocab/");
            resourceContext.newNamespaceContext(context);

            FileWriter w = new FileWriter("output.ttl");
            final TurtleWriter writer = new TurtleWriter()
                    .setContext(context)
                    .output(w);
            writer.writeNamespaces();


            ImportService service = new ImportService()
                    .threads(threads)
                    .factory(
                            new ImporterFactory() {
                                @Override
                                public Importer newImporter() {
                                    return new ArticleDB(writer);
                                }
                            }).execute().shutdown();

            logger.info("finished, number of files = {}, resources indexed = {}",
                    fileCounter, resourceCounter);

            service.shutdown();
            w.close();
            //es.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    public ArticleDB(RDFSerializer serializer) {
        this.serializer = serializer;
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

    private void push(URI uri) throws Exception {
        if (uri == null) {
            return;
        }
        InputStream in = factory.getInputStream(uri);
        if (in == null) {
            throw new IOException("unable to open " + uri);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            JsonParser parser = jsonFactory.createJsonParser(reader);
            JsonToken token = parser.nextToken();
            Resource resource = null;
            String key = null;
            String value = null;
            while (token != null) {
                switch (token) {
                    case START_OBJECT: {
                        resource = resourceContext.newResource();
                        break;
                    }
                    case END_OBJECT: {
                        synchronized (serializer) {
                            serializer.write(resource);
                        }
                        resource = null;
                        break;
                    }
                    case START_ARRAY: {
                        logger.info("start of file {}", uri);
                        break;
                    }
                    case END_ARRAY: {
                        logger.info("end of file {}", uri);
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
                            URI coins = URI.create("http://localhost/?" + XMLUtil.unescape(value));
                            resource.add("rdf:type", IRI.create("bf:Article"))
                                    .add("rdf:type", IRI.create("fabio:Article"))
                                    .add("rdf:type", IRI.create("frbr:Expression"));
                            final Resource r = resource;
                            URIListener l = new URIListener() {

                                String aufirst = null;
                                String aulast = null;

                                String spage = null;
                                String epage = null;

                                @Override
                                public void received(String k, String v) {
                                    v = v.trim();
                                    if (v.isEmpty()) {
                                        return;
                                    }
                                    switch (k) {
                                        case "rft_id" : {
                                            IRI iri = IRI.create(v);
                                            String doi = iri.getSchemeSpecificPart();
                                            if (doi.startsWith("doi/")) {
                                                doi = doi.substring(4);
                                            }
                                            r.id(IRI.create("http://xbib.org/works#" + doi));
                                            r.add("dcterms:identifier", iri)
                                             .add("prism:doi", doi);
                                            break;
                                        }
                                        case "rft.atitle" : {
                                            r.add("dc:title", v);
                                            break;
                                        }
                                        case "rft.jtitle" : {
                                            r.newResource("dcterms:isPartOf")
                                                    .add("rdf:type", IRI.create("bf:Serial"))
                                                    .add("dc:title", v);
                                            r.add("prism:publicationName", v);
                                            break;
                                        }
                                        case "rft.genre" : {
                                            r.add("dc:type", v);
                                            break;
                                        }
                                        case "rft.aulast" : {
                                            if (aufirst != null) {
                                                r.newResource("dc:creator")
                                                        .add("rdf:type", IRI.create("foaf:Agent"))
                                                        .add("foaf:familyName", aulast)
                                                        .add("foaf:givenName", aufirst);
                                                aulast = null;
                                                aufirst = null;
                                            } else {
                                                aufirst = v;
                                            }
                                            break;
                                        }
                                        case "rft.aufirst" : {
                                            if (aulast != null) {
                                                r.newResource("dc:creator")
                                                        .add("rdf:type", IRI.create("foaf:Agent"))
                                                        .add("foaf:familyName", aulast)
                                                        .add("foaf:givenName", aufirst);
                                                aulast = null;
                                                aufirst = null;
                                            } else {
                                                aulast = v;
                                            }
                                            break;
                                        }
                                        case "rft.au" : {
                                            r.add("dc:creator", v);
                                            break;
                                        }
                                        case "rft.date" : {
                                            Literal l = new SimpleLiteral<>(v).type(Literal.GYEAR);
                                            r.add("fabio:hasPublicationYear", v)
                                                    .add("prism:publicationDate", l);
                                            break;
                                        }
                                        case "rft.volume" : {
                                            r.newResource("frbr:embodiment")
                                                    .add("rdf:type", IRI.create("fabio:PeriodicalVolume"))
                                                    .add("prism:volume", v);
                                            break;
                                        }
                                        case "rft.issue" : {
                                            r.newResource("frbr:embodiment")
                                                    .add("rdf:type", IRI.create("fabio:PeriodicalIssue"))
                                                    .add("prism:issueIdentifier", v);
                                            break;
                                        }
                                        case "rft.spage" : {
                                            if (spage != null) {
                                                r.newResource("frbr:embodiment")
                                                    .add("rdf:type", IRI.create("fabio:PrintObject"))
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
                                                        .add("rdf:type", IRI.create("fabio:PrintObject"))
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
                                    if (aufirst != null || aulast != null) {
                                        r.newResource("dc:creator")
                                                .add("rdf:type", IRI.create("foaf:Agent"))
                                                .add("foaf:familyName", aulast)
                                                .add("foaf:givenName", aufirst);
                                    }
                                    if (spage != null || epage != null) {
                                        r.newResource("frbr:embodiment")
                                                .add("rdf:type", IRI.create("fabio:PrintObject"))
                                                .add("prism:startingPage", spage)
                                                .add("prism:endingPage", epage);
                                    }
                                }
                            };
                            URIUtil.parseQueryString(coins, "UTF-8", l);
                            l.close();
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

    interface URIListener extends URIUtil.ParameterListener {
        void close();
    }
}
