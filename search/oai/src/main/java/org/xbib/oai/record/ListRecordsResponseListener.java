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
package org.xbib.oai.record;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xbib.date.DateUtil;
import org.xbib.io.http.HttpResponse;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.DefaultOAIResponseListener;
import org.xbib.oai.util.MetadataHandler;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.util.RecordHeader;
import org.xbib.oai.util.ResumptionToken;
import org.xbib.xml.XMLFilterReader;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ListRecordsResponseListener extends DefaultOAIResponseListener {

    private final Logger logger = LoggerFactory.getLogger(ListRecordsResponseListener.class.getName());

    private final ListRecordsRequest request;

    private ListRecordsResponse response;

    private List<MetadataHandler> metadataHandlers = new ArrayList();

    private ResumptionToken token;

    public ListRecordsResponseListener(ListRecordsRequest request) {
        super(request);
        this.request = request;
    }

    public ListRecordsResponseListener register(MetadataHandler metadataHandler) {
        metadataHandlers.add(metadataHandler);
        return this;
    }

    public ResumptionToken getResumptionToken() {
        return token;
    }

    @Override
    public ListRecordsResponse getResponse() {
        return response;
    }

    @Override
    public void receivedResponse(HttpResponse result) throws IOException {
        super.receivedResponse(result);
        this.response = new ListRecordsResponse(request);
        int status = result.getStatusCode();
        if (status == 503) {
            String retryAfter = result.getHeaders().get("retry-after").get(0);
            if (retryAfter != null) {
                logger.info("got retry-after {}", retryAfter);
                if (isDigits(retryAfter)) {
                    // retry-after is given in seconds
                    response.setExpire(Integer.parseInt(retryAfter));
                } else {
                    Date d = DateUtil.parseDateRFC(retryAfter);
                    if (d != null) {
                        response.setExpire(d.getTime() - new Date().getTime());
                    }
                }
            }
            return;
        }
        if (!result.ok()) {
            throw new IOException(status + " " + result.getThrowable());
        }
        // XML content type?
        if (!result.getContentType().endsWith("xml")) {
            throw new IOException("answer to " + request
                    + " does not have XML content type: "
                    + result.getContentType());
        }
        Reader r = new StringReader(result.getBody());
        XMLFilterReader reader = new ListRecordsFilterReader(response);
        InputSource source = new InputSource(r);
        response.getTransformer().setSource(reader, source);
    }

    private boolean isDigits(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    class ListRecordsFilterReader extends XMLFilterReader {

        private final ListRecordsResponse response;

        private StringBuilder content = new StringBuilder();

        private RecordHeader header;

        private boolean inMetadata = false;

        ListRecordsFilterReader(ListRecordsResponse response) {
            this.response = response;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            request.setResumptionToken(null);
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void startElement(String uri, String localname, String qname, Attributes atts)
                throws SAXException {
            super.startElement(uri, localname, qname, atts);
            if (OAIConstants.NS_URI.equals(uri)) {
                switch (localname) {
                    case "error": {
                        response.setError(atts.getValue("code"));
                        break;
                    }
                    case "metadata": {
                        inMetadata = true;
                        for (MetadataHandler mh : metadataHandlers) {
                            mh.startDocument();
                        }
                        break;
                    }
                    case "resumptionToken": {
                        try {
                            token = ResumptionToken.newToken(null);
                            String cursor = atts.getValue("cursor");
                            if (cursor != null) {
                                token.setCursor(Integer.parseInt(cursor));
                            }
                            String completeListSize = atts.getValue("completeListSize");
                            if (completeListSize != null) {
                                token.setCompleteListSize(Integer.parseInt(completeListSize));
                            }
                            if (!token.isComplete()) {
                                request.setResumptionToken(token);
                            }
                        } catch (Exception e) {
                            throw new SAXException(e);
                        }
                        break;
                    }
                    case "header": {
                        header = new RecordHeader();
                        break;
                    }
                }
                return;
            }
            if (inMetadata) {
                for (MetadataHandler mh : metadataHandlers) {
                    mh.startElement(uri, localname, qname, atts);
                }
            }
        }

        @Override
        public void endElement(String nsURI, String localname, String qname) throws SAXException {
            super.endElement(nsURI, localname, qname);
            if (OAIConstants.NS_URI.equals(nsURI)) {
                switch (localname) {
                    case "metadata": {
                        for (MetadataHandler mh : metadataHandlers) {
                            mh.endDocument();
                        }
                        inMetadata = false;
                        break;
                    }
                    case "responseDate": {
                        Date d = DateUtil.parseDateISO(content.toString());
                        response.setResponseDate(d);
                        break;
                    }
                    case "resumptionToken": {
                        if (token != null && content != null && content.length() > 0) {
                            token.setValue(content.toString());
                            // feedback to request
                            request.setResumptionToken(token);
                        } else {
                            // some servers send a null or an empty token as last token
                            token = null;
                            request.setResumptionToken(null);
                        }
                        break;
                    }
                    case "header": {
                        for (MetadataHandler mh : metadataHandlers) {
                            mh.setHeader(header);
                        }
                        header = new RecordHeader();
                        break;
                    }
                    case "identifier": {
                        if (header != null && content != null && content.length() > 0) {
                            String id = content.toString().trim();
                            header.setIdentifier(id);
                        }
                        break;
                    }
                    case "datestamp": {
                        if (header != null && content != null && content.length() > 0) {
                            header.setDatestamp(DateUtil.parseDateISO(content.toString().trim()));
                        }
                        break;
                    }
                    case "setSpec": {
                        if (header != null && content != null && content.length() > 0) {
                            header.setSetspec(content.toString().trim());
                        }
                        break;
                    }
                }
                content.setLength(0);
                return;
            }
            if (inMetadata) {
                for (MetadataHandler mh : metadataHandlers) {
                    mh.endElement(nsURI, localname, qname);
                }
            }
            content.setLength(0);
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            super.characters(chars, start, length);
            content.append(new String(chars, start, length).trim());
            if (inMetadata) {
                for (MetadataHandler mh : metadataHandlers) {
                    mh.characters(chars, start, length);
                }
            }
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            super.startPrefixMapping(prefix, uri);
            if (inMetadata) {
                for (MetadataHandler mh : metadataHandlers) {
                    mh.startPrefixMapping(prefix, uri);
                }
            }
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            super.endPrefixMapping(prefix);
            if (inMetadata) {
                for (MetadataHandler mh : metadataHandlers) {
                    mh.endPrefixMapping(prefix);
                }
            }
        }
    }

}
