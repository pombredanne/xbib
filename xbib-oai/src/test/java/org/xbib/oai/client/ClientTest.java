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
package org.xbib.oai.client;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;
import org.xbib.io.EmptyWriter;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.IdentifyRequest;
import org.xbib.oai.IdentifyResponse;
import org.xbib.oai.ListRecordsRequest;
import org.xbib.oai.ListRecordsResponse;
import org.xbib.oai.MetadataPrefixService;
import org.xbib.oai.MetadataReader;
import org.xbib.oai.OAIOperation;
import org.xbib.oai.ResumptionToken;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.io.XmlTriplifier;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ClientTest {

    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class.getName());

    public void testZDBClient() throws Exception {
        OAIClient client = OAIClientFactory.getClient("ZDB");
        IdentifyRequest request = new IdentifyRequest(client.getURI());
        StringWriter sw = new StringWriter();
        IdentifyResponse response = new IdentifyResponse(sw);
        StylesheetTransformer transformer = new StylesheetTransformer("xsl");
        client.setStylesheetTransformer(transformer);
        client.prepareIdentify(request, response);
    }
    private Resource resource;

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    @Test
    public void testOAIClient() throws Exception {
        OAIClient client = OAIClientFactory.getClient(
                 "http://services.dnb.de/oai/repository"
        );
        ListRecordsRequest request = new MyListRecordsRequest(client.getURI()); 
        StylesheetTransformer transformer = new StylesheetTransformer("src/test/resources/xsl");
        boolean failure = false;
        do {
            EmptyWriter sw = new EmptyWriter();
            ListRecordsResponse response = new ListRecordsResponse(sw);
            client.setStylesheetTransformer(transformer);
            //client.setProxy("localhost", 3128);
            final XmlTriplifier reader = MetadataPrefixService.getTriplifier(request.getMetadataPrefix());
            final StatementListener listener = new StatementListener() {

                @Override
                public void newIdentifier(URI uri) {
                    getResource().id(uri);
                }

                @Override
                public void statement(Statement statement) {
                    getResource().add(statement);
                }
            };
            reader.setListener(listener);
            final DefaultHandler handler = reader.getHandler();
            MetadataReader metadataReader = new MetadataReader() {

                @Override
                public void startDocument() throws SAXException {
                    handler.startDocument();
                    setResource(new SimpleResource());
                }

                @Override
                public void endDocument() throws SAXException {
                    handler.endDocument();
                    if (resource.id() == null) {
                        resource.id(URI.create(getHeader().getIdentifier()));
                    }
                    StringWriter sw = new StringWriter();
                    TurtleWriter t = new TurtleWriter();
                    try {
                        t.write(getResource(), true, sw);
                        logger.info(sw.toString());
                    } catch (IOException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }

                @Override
                public void startPrefixMapping(String prefix, String uri) throws SAXException {
                    handler.startPrefixMapping(prefix, uri);
                }

                @Override
                public void endPrefixMapping(String prefix) throws SAXException {
                    handler.endPrefixMapping(prefix);
                }

                @Override
                public void startElement(String ns, String localname, String qname, Attributes atrbts) throws SAXException {
                    handler.startElement(ns, localname, qname, atrbts);
                }

                @Override
                public void endElement(String ns, String localname, String qname) throws SAXException {
                    handler.endElement(ns, localname, qname);
                }

                @Override
                public void characters(char[] chars, int pos, int len) throws SAXException {
                    handler.characters(chars, pos, len);
                }
            };
            client.setMetadataReader(metadataReader);
            try {
                client.prepareListRecords(request, response);
                OAIOperation op = client.execute(30, TimeUnit.SECONDS);
                logger.info("OAI results " + op.getResults());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                failure = true;
            }
        } while (request.getResumptionToken() != null && !failure);
    }

    private class MyListRecordsRequest extends ListRecordsRequest {
        
            ResumptionToken token;

            public MyListRecordsRequest(URI uri) {
                super(uri);
            }
            @Override
            public void setFrom(Date from) {
            }

            @Override
            public Date getFrom() {
                //return DateUtil.parseDateISO("2012-01-23T11:00:00Z");
                return null;
            }

            @Override
            public void setUntil(Date until) {
            }

            @Override
            public Date getUntil() {
                //return DateUtil.parseDateISO("2012-01-23T12:00:00Z");
                return null;
            }

            @Override
            public String getSet() {
                //return "authorities";
                //return "rheinmono";
                return "bib";
            }

            @Override
            public String getMetadataPrefix() {
                //return "RDFxml";
                //return "oai_dc";
                return "PicaPlus-xml";
            }

            @Override
            public void setResumptionToken(ResumptionToken token) {
                this.token = token;
            }

            @Override
            public ResumptionToken getResumptionToken() {
                return token;
            }

            @Override
            public String getPath() {
                return null;
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return null;
            }

    }

}
