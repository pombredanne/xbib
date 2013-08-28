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
package org.xbib.tools.harvest;

import org.xbib.date.DateUtil;
import org.xbib.io.NullWriter;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.record.ListRecordsRequest;
import org.xbib.oai.record.ListRecordsResponseListener;
import org.xbib.oai.util.rdf.RdfMetadataHandler;
import org.xbib.oai.util.rdf.RdfOutput;
import org.xbib.oai.util.rdf.RdfResourceHandler;
import org.xbib.rdf.Property;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.ntriple.NTripleWriter;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleProperty;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.xml.XSD;

import javax.xml.namespace.QName;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import static org.xbib.tools.opt.util.DateConverter.datePattern;

/**
 *
 * OAI harvester, write documents to RDF
 *
 * @author Jörg Prante <joergprante@gmail.com>
 */
public class DOAJ {

    private final static Logger logger = LoggerFactory.getLogger(DOAJ.class.getSimpleName());

    private static OptionSet options;

    private OAIClient client;

    private final static AtomicLong counter = new AtomicLong(0L);

    private final OutputStream out;

    private String output;

    public static void main(String[] args) throws Exception {
        int exitcode = 0;
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("server").withRequiredArg().ofType(String.class).required();
                    accepts("set").withRequiredArg().ofType(String.class);
                    accepts("prefix").withRequiredArg().ofType(String.class);
                    accepts("from").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd'T'hh:mm:ss'Z'"));
                    accepts("until").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd'T'hh:mm:ss'Z'")).defaultsTo(new Date());
                    accepts("fromDate").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd"));
                    accepts("untilDate").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd")).defaultsTo(new Date());
                    accepts("output").withOptionalArg().ofType(String.class).defaultsTo("oai.ttl");
                }
            };
            options = parser.parse(args);

            String server = (String) options.valueOf("server");
            String prefix = (String) options.valueOf("prefix");
            Date from = options.valueOf("fromDate") != null?
                    (Date) options.valueOf("fromDate") :  (Date) options.valueOf("from");
            Date until = options.valueOf("untilDate") != null?
                    (Date) options.valueOf("untilDate") :  (Date) options.valueOf("until");
            String output = (String) options.valueOf("output");

            final OAIClient client = OAIClientFactory.newClient(server);

            ListRecordsRequest request = client.newListRecordsRequest()
                    .setMetadataPrefix(prefix)
                    .setFrom(from)
                    .setUntil(until);

            new DOAJ(client, output).execute(request).close();

            logger.info("harvested {} documents", counter.get());
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    DOAJ(OAIClient client, String output) throws IOException {
        this.client = client;
        this.output = output;
        String fileName = DateUtil.today() + "_" + output + ".gz";
        FileOutputStream fout = new FileOutputStream(fileName);
        this.out = new GZIPOutputStream(fout){
            {
                def.setLevel(Deflater.BEST_COMPRESSION);
            }
        };
    }

    private DOAJ execute(ListRecordsRequest request) throws Exception {
        final RdfMetadataHandler metadataHandler = new RdfMetadataHandler();
        final RdfResourceHandler resourceHandler = new DOAJResourceHandler(metadataHandler.getResourceContext());
        final RdfOutput rdfout =
                new TurtleOutput(metadataHandler.getContext());
        metadataHandler.setHandler(resourceHandler)
                .setOutput(rdfout);
        try {
            do {
                ListRecordsResponseListener listener = new ListRecordsResponseListener(request)
                        .register(metadataHandler);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    //StringWriter sw = new StringWriter();
                    NullWriter sw = new NullWriter();
                    listener.getResponse().to(sw);
                    //logger.info(sw.toString());
                }
                request = listener.isFailure() ? null : client.resume(request, listener.getResumptionToken());
            } while (request != null);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    private void close() throws IOException {
        out.close();
        client.close();
    }

    // DOAJ specific

    private final IRI ISSN = IRI.create("urn:ISSN");

    private final IRI EISSN = IRI.create("urn:EISSN");

    private final IRI LCCN = IRI.create("urn:LCC");

    class DOAJResourceHandler extends RdfResourceHandler {

        public DOAJResourceHandler(ResourceContext context) {
            super(context);
        }

        /**
         * Map properties in DOAJ
         *
         * @param property
         * @return
         */
        @Override
        public Property toProperty(Property property) {
            if ("issn".equals(property.id().getSchemeSpecificPart())) {
                return new SimpleProperty(IRI.builder().curi("dc", "identifier").build());
            }
            if ("eissn".equals(property.id().getSchemeSpecificPart())) {
                return new SimpleProperty(IRI.builder().curi("dc", "identifier").build());
            }
            return property;
        }

        /**
         * Convert values in DOAJ
         *
         * @param name
         * @param content
         * @return
         */
        @Override
        public Object toObject(QName name, String content) {
            switch (name.getLocalPart()) {
                case "identifier" : {
                    if (content.startsWith("http://")) {
                        return new SimpleLiteral(content).type(XSD.ANYURI);
                    }
                    if (content.startsWith("issn: ")) {
                        return new SimpleLiteral(content.substring(6)).type(ISSN);
                    }
                    if (content.startsWith("eissn: ")) {
                        return new SimpleLiteral(content.substring(7)).type(EISSN);
                    }
                    break;
                }
                case "subject" : {
                    if (content.startsWith("LCC: ")) {
                        return new SimpleLiteral(content.substring(5)).type(LCCN);
                    }
                    break;
                }
                case "issn" : {
                    return new SimpleLiteral(content.substring(0,4) + "-" + content.substring(4)).type(ISSN);
                }
                case "eissn" : {
                    return new SimpleLiteral(content.substring(0,4) + "-" + content.substring(4)).type(EISSN);
                }
            }
            return super.toObject(name, content);
        }
    }

    class TurtleOutput extends RdfOutput {

        TurtleWriter writer;

        TurtleOutput(IRINamespaceContext context) throws IOException {
            this.writer = new TurtleWriter()
                    .output(out)
                    .setContext(context)
                    .writeNamespaces();
        }

        @Override
        public RdfOutput output(ResourceContext resourceContext) throws IOException {
            writer.write(resourceContext.resource());
            counter.incrementAndGet();
            return this;
        }
    }

    class NTripleOutput extends RdfOutput {

        NTripleWriter writer;

        NTripleOutput(IRINamespaceContext context) throws IOException {
            this.writer = new NTripleWriter()
                    .output(out);
        }

        @Override
        public RdfOutput output(ResourceContext resourceContext) throws IOException {
            writer.write(resourceContext.resource());
            counter.incrementAndGet();
            return this;
        }
    }
}
