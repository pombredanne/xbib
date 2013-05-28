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

import org.xbib.io.http.netty.HttpResponse;
import org.xbib.io.http.netty.HttpResponseListener;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.OAIRequest;
import org.xbib.oai.OAIResponse;
import org.xbib.xml.XMLFilterReader;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;

public abstract class AbstractResponseListener implements HttpResponseListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractResponseListener.class.getName());
    protected final StylesheetTransformer transformer;
    private final OAIResponse response;
    private HttpResponse httpResponse;
    private int statuscode;

    AbstractResponseListener(OAIRequest request, OAIResponse response, StylesheetTransformer transformer) {
        this.response = response;
        this.transformer = transformer;
    }

    @Override
    public void receivedResponse(HttpResponse result) {
        if (result != null) {
            httpResponse = result;
            statuscode = result.getStatusCode();
            if (statuscode >= 500 && statuscode < 600) {
                logger.error("HTTP error: {}", result.getBody());
                return;
            }
            try {
                XMLFilterReader reader = new OAIResponseFilterReader();
                InputSource source = new InputSource(new StringReader(result.getBody()));
                StreamResult streamResult = response.getOutput() != null
                        ? new StreamResult(response.getOutput())
                        : new StreamResult(response.getWriter());
                transformer.setSource(reader, source).setResult(streamResult).transform();
            } catch (TransformerException e) {

            }
        }
    }

    public HttpResponse getResponse() {
        return httpResponse;
    }

    class OAIResponseFilterReader extends XMLFilterReader {

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localname, String qname, Attributes atts) throws SAXException {
        }

        @Override
        public void endElement(String uri, String localname, String qname) throws SAXException {
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }
    }
}
