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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Date;
import java.util.Map;

import org.xbib.elasticsearch.ElasticsearchIndexerMock;
import org.xbib.io.EmptyWriter;
import org.xbib.io.util.DateUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.ListRecordsRequest;
import org.xbib.oai.ListRecordsResponse;
import org.xbib.oai.MetadataReader;
import org.xbib.oai.ResumptionToken;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.io.rdfxml.RdfXmlReader;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ElasticsearchOAITest {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchOAITest.class.getName());
    private Resource resource;

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public void testDNBOAI() throws Exception {

        final ElasticsearchIndexerMock es = new ElasticsearchIndexerMock()
                .setIndex("test")
                .setType("test");

        OAIClient client = OAIClientFactory.getClient("DNB");
        ListRecordsRequest request = new OAIListRecordsRequest(client.getURI());
        StylesheetTransformer transformer = new StylesheetTransformer("src/main/resources/xsl");
        do {

            EmptyWriter w = new EmptyWriter();
            ListRecordsResponse response = new ListRecordsResponse(w);
            client.setStylesheetTransformer(transformer);
            //client.setProxy("localhost", 3128);
            RdfXmlReader reader = new RdfXmlReader();
            final StatementListener stmt = new StatementListener() {

                @Override
                public void newIdentifier(IRI uri) {
                    getResource().id(uri);
                }

                @Override
                public void statement(Statement statement) {
                    getResource().add(statement);
                }
            };
            reader.setListener(stmt);
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
                    //es.output(null); // TODO
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
            client.setMetadataReader(metadataReader);
            client.prepareListRecords(request, response).execute();
        } while (request.getResumptionToken() != null);
        es.shutdown();
    }
    
    class OAIListRecordsRequest extends ListRecordsRequest {
            ResumptionToken token;
            Date from;
            Date until;
            
            public OAIListRecordsRequest(URI uri) {
                super(uri);
            }
                    
            @Override
                    public void setFrom(Date from) {
                        this.from = from;
                    }

            @Override
            public Date getFrom() {
                return DateUtil.parseDateISO("2012-01-23T00:00:00Z");
            }

            public void setUntil(Date until) {
                this.until = until;
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
