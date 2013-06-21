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

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import org.testng.annotations.Test;
import org.xbib.date.DateUtil;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.identify.IdentifyRequest;
import org.xbib.oai.identify.IdentifyResponseListener;
import org.xbib.oai.record.ListRecordsRequest;
import org.xbib.oai.record.ListRecordsResponseListener;
import org.xbib.oai.util.MetadataPrefixService;
import org.xbib.oai.util.MetadataHandler;
import org.xbib.rdf.Triple;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.XmlTriplifier;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DNBClientTest {

    private final Logger logger = LoggerFactory.getLogger(DNBClientTest.class.getName());

    @Test
    public void testIdentify() throws Exception {
        logger.info("trying to connect to ZDB for Identify request");
        OAIClient client = OAIClientFactory.newClient("ZDB");
        IdentifyRequest request = client.newIdentifyRequest();
        request.prepare().execute(new IdentifyResponseListener() {}).waitFor();
    }

    @Test
    public void testListRecordsDOAJ() throws Exception {
        logger.info("trying to connect to DNB");

        OAIClient client = OAIClientFactory.newClient("http://services.dnb.de/oai/repository");

        ListRecordsRequest request =  client.newListRecordsRequest()
                .setFrom(DateUtil.parseDateISO("2013-01-01T00:00:00Z"))
                .setUntil(DateUtil.parseDateISO("2013-01-10T00:00:00Z"))
                .setSet("bib")
                .setMetadataPrefix("PicaPlus-xml");

        final XmlTriplifier reader = MetadataPrefixService.getTriplifier(request.getMetadataPrefix());
        final TripleListener triples = new TripleListener() {

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
                //getResource().id(uri);
                return this;
            }

            @Override
            public TripleListener triple(Triple statement) {
                //getResource().add(statement);
                return this;
            }
        };
        reader.setTripleListener(triples);
        MetadataHandler metadataHandler = new MetadataHandler() {

            @Override
            public void startDocument() throws SAXException {
                //handler.startDocument();
                //setResource(new SimpleResource());
                logger.info("startDocument");
            }

            @Override
            public void endDocument() throws SAXException {
                logger.info("endDocument");
                //handler.endDocument();
                //if (resource.id() == null) {
                //    resource.id(IRI.builder().host("test").query("test").fragment(getHeader().getIdentifier()).build());
                //}
                    /*StringWriter sw = new StringWriter();
                    try {
                        TurtleWriter t = new TurtleWriter().output(sw);
                        t.write(getResource());
                        t.close();
                        logger.info(sw.toString());
                    } catch (IOException ex) {
                        logger.error(ex.getMessage(), ex);
                    }*/
            }

            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException {
                //handler.startPrefixMapping(prefix, uri);
            }

            @Override
            public void endPrefixMapping(String prefix) throws SAXException {
                //handler.endPrefixMapping(prefix);
            }

            @Override
            public void startElement(String ns, String localname, String qname, Attributes atrbts) throws SAXException {
                //handler.startElement(ns, localname, qname, atrbts);
            }

            @Override
            public void endElement(String ns, String localname, String qname) throws SAXException {
                //handler.endElement(ns, localname, qname);
            }

            @Override
            public void characters(char[] chars, int pos, int len) throws SAXException {
                //handler.characters(chars, pos, len);
            }
        };
        FileWriter sw = new FileWriter("target/bib.xml");
        try {
            do {
                ListRecordsResponseListener listener = new ListRecordsResponseListener(request)
                    .register(metadataHandler);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    listener.getResponse().to(sw);
                }
                request = listener.isFailure() ? null :
                        client.resume(request, listener.getResumptionToken());
            } while (request != null);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        sw.close();
    }

}
