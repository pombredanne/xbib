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
import org.xbib.date.DateUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.ListRecordsRequest;
import org.xbib.oai.ListRecordsResponse;
import org.xbib.oai.MetadataReader;
import org.xbib.oai.OAI;
import org.xbib.oai.RecordHeader;
import org.xbib.oai.ResumptionToken;
import org.xbib.oai.exceptions.BadArgumentException;
import org.xbib.oai.exceptions.BadResumptionTokenException;
import org.xbib.oai.exceptions.NoRecordsMatchException;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.xml.XMLFilterReader;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

public class ListRecordsResponseListener extends AbstractResponseListener {

    private static final Logger logger = LoggerFactory.getLogger(ListRecordsResponseListener.class.getName());
    private final ListRecordsRequest request;
    private final ListRecordsResponse response;
    private MetadataReader metadataReader;

    ListRecordsResponseListener(ListRecordsRequest request, ListRecordsResponse response, StylesheetTransformer transformer) {
        super(request, response, transformer);
        this.request = request;
        this.response = response;
    }

    public ListRecordsResponseListener setMetadataReader(MetadataReader metadataReader) {
        this.metadataReader = metadataReader;
        return this;
    }

    @Override
    public void receivedResponse(HttpResponse result) {
        try {
            if (result == null) {
                throw new IOException("result is empty");
            }
            int status = result.getStatusCode();
            if (status == 503) {
                String retryAfter = result.getHeaders().getFirstValue("retry-after");
                if (retryAfter != null) {
                    logger.debug("got retry-after {}", retryAfter);
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
            if (status >= 200 && status < 300) {
                XMLFilterReader reader = new ListRecordsFilterReader();
                InputSource source = new InputSource(new StringReader(result.getBody()));
                StreamResult streamResult = response.getOutput() != null
                        ? new StreamResult(response.getOutput())
                        : new StreamResult(response.getWriter());
                transformer.setSource(reader, source).setResult(streamResult).transform();
                // check for OAI errors
                if ("noRecordsMatch".equals(response.getError())) {
                    throw new NoRecordsMatchException("metadataPrefix=" + request.getMetadataPrefix()
                            + ",set=" + request.getSet()
                            + ",from=" + DateUtil.formatDateISO(request.getFrom())
                            + ",until=" + DateUtil.formatDateISO(request.getUntil()));
                } else if ("badResumptionToken".equals(response.getError())) {
                    throw new BadResumptionTokenException(request.getResumptionToken());
                } else if ("badArgument".equals(response.getError())) {
                    throw new BadArgumentException();
                } else if (response.getError() != null) {
                    throw new OAIException(response.getError());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
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

        private StringBuilder content = new StringBuilder();
        private RecordHeader header;
        private boolean inMetadata = false;

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
            if (OAI.NS_URI.equals(uri)) {
                switch (localname) {
                    case "error": {
                        response.setError(atts.getValue("code"));
                        break;
                    }
                    case "metadata": {
                        inMetadata = true;
                        if (metadataReader != null) {
                            metadataReader.startDocument();
                        }
                        break;
                    }
                    case "resumptionToken": {
                        try {
                            ResumptionToken token = ResumptionToken.newToken(null);
                            String cursor = atts.getValue("cursor");
                            if (cursor != null) {
                                token.setCursor(Integer.parseInt(cursor));
                            }
                            String completeListSize = atts.getValue("completeListSize");
                            if (completeListSize != null) {
                                token.setCompleteListSize(Integer.parseInt(completeListSize));
                            }
                            request.setResumptionToken(token);
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
            if (inMetadata && metadataReader != null) {
                metadataReader.startElement(uri, localname, qname, atts);
            }
        }

        @Override
        public void endElement(String uri, String localname, String qname) throws SAXException {
            super.endElement(uri, localname, qname);
            if (OAI.NS_URI.equals(uri)) {
                switch (localname) {
                    case "metadata": {
                        if (metadataReader != null) {
                            metadataReader.endDocument();
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
                        if (content != null && content.length() > 0) {
                            request.setResumptionToken(request.getResumptionToken().setValue(content.toString()));
                        } else {
                            // some servers send an empty token as last token, we need to clean out the resuming
                            request.setResumptionToken(null);
                        }
                        break;
                    }
                    case "identifier": {
                        if (header != null && content != null && content.length() > 0) {
                            header.setIdentifier(content.toString().trim());
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
                    case "header": {
                        metadataReader.setHeader(header);
                        header = new RecordHeader();
                        break;
                    }
                }
                content.setLength(0);
                return;
            }
            if (inMetadata && metadataReader != null) {
                metadataReader.endElement(uri, localname, qname);
            }
            content.setLength(0);
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            super.characters(chars, start, length);
            content.append(new String(chars, start, length).trim());
            if (inMetadata && metadataReader != null) {
                metadataReader.characters(chars, start, length);
            }
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            super.startPrefixMapping(prefix, uri);
            if (inMetadata && metadataReader != null) {
                metadataReader.startPrefixMapping(prefix, uri);
            }
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            super.endPrefixMapping(prefix);
            if (inMetadata && metadataReader != null) {
                metadataReader.endPrefixMapping(prefix);
            }
        }
    }
}
