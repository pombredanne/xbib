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
import java.io.StringReader;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.xbib.io.http.netty.HttpResult;
import org.xbib.io.http.netty.HttpResultProcessor;
import org.xbib.oai.OAIRequest;
import org.xbib.oai.OAIResponse;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xbib.xml.transform.XMLFilterReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class AbstractResponseProcessor implements HttpResultProcessor {

    private final OAIRequest request;
    private final OAIResponse response;
    private final StylesheetTransformer transformer;
    private int statuscode;

    AbstractResponseProcessor(OAIRequest request, OAIResponse response, StylesheetTransformer transformer) {
        this.request = request;
        this.response = response;
        this.transformer = transformer;
    }

    @Override
    public void process(HttpResult result) throws IOException {
        if (result == null) {
            throw new IOException("result is empty");
        }
        statuscode = result.getStatusCode();
        try {
            XMLFilterReader reader = new OAIResponseFilterReader();
            InputSource source = new InputSource(new StringReader(result.getBody()));
            StreamResult target = response.getOutput() != null
                    ? new StreamResult(response.getOutput())
                    : new StreamResult(response.getWriter());
            transformer.setSource(reader, source).setTarget(target).apply();
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void processError(HttpResult result) throws IOException {
        statuscode = result.getStatusCode();
        throw new IOException(statuscode + " " + result.getBody());
    }
    
    public int getStatusCode() {
        return statuscode;
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
