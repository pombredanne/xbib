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
package org.xbib.oai;

import org.xbib.io.OutputFormat;
import org.xbib.io.http.netty.DefaultHttpResponse;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.xml.XMLFilterReader;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;

/**
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class DefaultOAIResponse
        extends DefaultHttpResponse
        implements OAIResponse, XMLEventConsumer {

    private final Logger logger = LoggerFactory.getLogger(DefaultOAIResponse.class.getName());

    private static final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    private OAIRequest request;

    private Reader reader;

    private XMLEventWriter eventWriter;

    private String errorCode;

    private Date responseDate;

    private long expire;

    private Writer writer;

    private StylesheetTransformer transformer;

    private String[] stylesheets;

    private OutputFormat format;

    public DefaultOAIResponse(OAIRequest request) {
        this.request = request;
        this.transformer = new StylesheetTransformer("/xsl");
    }

    public DefaultOAIResponse(OAIRequest request, Writer writer) {
        this(request);
        this.writer = writer;
        try {
            eventWriter = outputFactory.createXMLEventWriter(writer);
        } catch (XMLStreamException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public DefaultOAIResponse(OAIRequest request, OutputStream out, String encoding)
            throws UnsupportedEncodingException {
        this(request, new OutputStreamWriter(out, encoding));
    }

    public OAIRequest getRequest() {
        return request;
    }

    public DefaultOAIResponse setReader(Reader reader) {
        this.reader = reader;
        return this;
    }

    @Override
    public DefaultOAIResponse setStylesheetTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public DefaultOAIResponse setStylesheets(String... stylesheets) {
        this.stylesheets = stylesheets;
        return this;
    }

    public StylesheetTransformer getTransformer() {
        return transformer;
    }

    public String[] getStylesheets() {
        return stylesheets;
    }

    @Override
    public DefaultOAIResponse setOutputFormat(OutputFormat format) {
        this.format = format;
        return this;
    }

    public OutputFormat getOutputFormat() {
        return format;
    }

    public Writer getWriter() {
        return writer;
    }

    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        try {
            if (eventWriter != null) {
                eventWriter.flush();
            }
        } catch (XMLStreamException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void add(XMLEvent xmle) throws XMLStreamException {
        if (eventWriter != null) {
            eventWriter.add(xmle);
        }
    }

    public void setError(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getError() {
        return errorCode;
    }

    public void setResponseDate(Date date) {
        this.responseDate = date;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public void setExpire(long millis) {
        this.expire = millis;
    }

    public long getExpire() {
        return expire;
    }

    @Override
    public DefaultOAIResponse to(HttpServletResponse response) throws IOException {
        return this;
    }

    @Override
    public DefaultOAIResponse to(Writer writer) throws IOException {
        try {
            XMLFilterReader filter = new OAIResponseFilterReader();
            InputSource source = new InputSource(reader);
            StreamResult streamResult = new StreamResult(writer);
            transformer.setSource(filter, source).setResult(streamResult).transform();
        } catch (TransformerException e) {
            throw new IOException(e.getMessage(), e);
        }
        return this;
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
