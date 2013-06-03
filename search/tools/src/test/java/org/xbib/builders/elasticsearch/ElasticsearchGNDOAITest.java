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
package org.xbib.builders.elasticsearch;

import org.testng.annotations.Test;
import org.xbib.date.DateUtil;
import org.xbib.elasticsearch.support.ingest.transport.MockTransportClientIngest;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.OAISession;
import org.xbib.oai.record.ListRecordsRequest;
import org.xbib.oai.record.ListRecordsResponseListener;
import org.xbib.oai.util.MetadataHandler;
import org.xbib.oai.util.ResumptionToken;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.rdfxml.RdfXmlReader;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

public class ElasticsearchGNDOAITest {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchGNDOAITest.class.getName());
    private Resource resource;

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    @Test
    public void testDNBOAI() throws Exception {

        final MockTransportClientIngest es = new MockTransportClientIngest()
                .setIndex("test")
                .setType("test");

        OAIClient client = OAIClientFactory.newClient("DNB");
        ListRecordsRequest request = new OAIListRecordsRequest(client);
        StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources/xsl");
        boolean failure = false;
        do {

            //NullWriter w = new NullWriter();
            //ListRecordsResponse response = new ListRecordsResponse(w);
            //client.setStylesheetTransformer(transformer);
            //client.setProxy("localhost", 3128);
            RdfXmlReader reader = new RdfXmlReader();
            final TripleListener stmt = new TripleListener() {

                @Override
                public TripleListener startPrefixMapping(String prefix, String uri) {
                    return this;
                }

                @Override
                public TripleListener endPrefixMapping(String prefix) {
                    return this;
                }

                @Override
                public TripleListener newIdentifier(IRI uri) {
                    getResource().id(uri);
                    return this;
                }

                @Override
                public TripleListener triple(Triple statement) {
                    getResource().add(statement);
                    return this;
                }
            };
            reader.setTripleListener(stmt);
            final XmlHandler handler = reader.getHandler();
            MetadataHandler metadataHandler = new MetadataHandler() {

                @Override
                public void startDocument() throws SAXException {
                    handler.startDocument();
                    setResource(new SimpleResource());
                }

                @Override
                public void endDocument() throws SAXException {
                    handler.endDocument();
                    // TODO
                    //es.output(null);
                    // for now, let's dump turtle.
                    try {
                        StringWriter sw = new StringWriter();
                        TurtleWriter t = new TurtleWriter()
                                .output(sw)
                                .write(getResource());
                        logger.info(sw.toString());
                    } catch (IOException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }

                @Override
                public void startPrefixMapping(String string, String string1) throws SAXException {
                    handler.startPrefixMapping(string, string1);
                }

                @Override
                public void endPrefixMapping(String string) throws SAXException {
                    handler.endPrefixMapping(string);
                }

                @Override
                public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
                    handler.startElement(ns, localname, string2, atrbts);
                }

                @Override
                public void endElement(String ns, String localname, String string2) throws SAXException {
                    handler.endElement(ns, localname, string2);
                }

                @Override
                public void characters(char[] chars, int i, int i1) throws SAXException {
                    handler.characters(chars, i, i1);
                }
            };
            /*client.setMetadataHandler(metadataHandler);
            try {
                client.prepareListRecords(request, response);
                client.execute(30, TimeUnit.SECONDS);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                failure = true;
            }*/
            StringWriter sw = new StringWriter();
            ListRecordsResponseListener listener = new ListRecordsResponseListener(request, sw);
            listener.register(metadataHandler);
            try {
                request.prepare().execute(listener).waitFor();
                logger.info("response = {}", sw);
                failure = listener.isFailure();
                request = client.resume(request, listener.getResponse());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                failure = true;
            }

        } while (request.getResumptionToken() != null && !failure);
        es.shutdown();
    }

    class OAIListRecordsRequest extends ListRecordsRequest {
        ResumptionToken token;
        Date from;
        Date until;

        public OAIListRecordsRequest(OAISession session) {
            super(session);
        }

        @Override
        public OAIListRecordsRequest setFrom(Date from) {
            this.from = from;
            return this;
        }

        @Override
        public Date getFrom() {
            return DateUtil.parseDateISO("2012-01-23T00:00:00Z");
        }

        public OAIListRecordsRequest setUntil(Date until) {
            this.until = until;
            return this;
        }

        @Override
        public Date getUntil() {
            return DateUtil.parseDateISO("2012-01-23T01:00:00Z");
        }

        @Override
        public String getSet() {
            return "authorities";
        }

        @Override
        public String getMetadataPrefix() {
            return "RDFxml";
        }

        @Override
        public OAIListRecordsRequest setResumptionToken(ResumptionToken token) {
            this.token = token;
            return this;
        }

        @Override
        public ResumptionToken getResumptionToken() {
            return token;
        }

    }
}
