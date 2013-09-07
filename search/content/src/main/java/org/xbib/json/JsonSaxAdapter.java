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
package org.xbib.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.xbib.common.xcontent.xml.XmlNamespaceContext;
import org.xbib.xml.ToQName;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.TreeSet;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NULL;

/**
 * Converts JSON to SAX events. It can be used either directly
 * <pre>
 *  <code>
 * 	ContentHandler ch = ...;
 * 	JsonSaxAdapter service = new JsonSaxAdapter("{\"name\":\"value\"}", ch);
 * 	service.parse();
 *  </code>
 *  </pre>
 *
 * <pre>
 *  <code>
 * 	Transformer transformer = TransformerFactory.newInstance().newTransformer();
 * 	InputSource source = new InputSource(...);
 * 	Result result = ...;
 * 	transformer.transform(new SAXSource(new JsonXmlReader(),source), result);
 *  </code>
 *  </pre>
 *
 */
public class JsonSaxAdapter {

    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

    private static final JsonFactory factory = new JsonFactory();

    private final JsonParser jsonParser;

    private final ContentHandler contentHandler;

    private QName root = new QName("root");

    private XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();

    public JsonSaxAdapter(Reader reader, ContentHandler contentHandler) throws IOException {
        this(factory.createParser(reader), contentHandler);
    }

    public JsonSaxAdapter(JsonParser jsonParser, ContentHandler contentHandler) {
        this.jsonParser = jsonParser;
        this.contentHandler = contentHandler;
        contentHandler.setDocumentLocator(new DocumentLocator());
    }

    public JsonSaxAdapter root(QName root) {
        this.root = root;
        return this;
    }

    public JsonSaxAdapter context(XmlNamespaceContext context) {
        this.context = context;
        return this;
    }

    /**
     * Method parses JSON and emits SAX events.
     */
    public void parse() throws IOException, SAXException {
        jsonParser.nextToken();
        contentHandler.startDocument();
        writeNamespaceDeclarations(context);
        if (root != null) {
            contentHandler.startElement(root.getNamespaceURI(), root.getLocalPart(),
                    root.getPrefix() + ":" + root.getLocalPart(), EMPTY_ATTRIBUTES);
        }
        parseObject();
        if (root != null) {
            contentHandler.endElement(root.getNamespaceURI(), root.getLocalPart(),
                    root.getPrefix() + ":" + root.getLocalPart());
        }
        contentHandler.endDocument();
    }

    /**
     * Parses generic object.
     *
     * @return number of elements written
     * @throws IOException
     * @throws SAXException
     */
    private int parseObject() throws SAXException, IOException {
        int elementsWritten = 0;
        while (jsonParser.nextToken() != null && jsonParser.getCurrentToken() != END_OBJECT) {
            if (FIELD_NAME.equals(jsonParser.getCurrentToken())) {
                String elementName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                parseElement(elementName);
                elementsWritten++;
            } else {
                throw new JsonParseException("expected field name, but got " + jsonParser.getCurrentToken(),
                        jsonParser.getCurrentLocation());
            }
        }
        return elementsWritten;
    }

    private void parseElement(final String elementName) throws SAXException, IOException {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (START_OBJECT.equals(currentToken)) {
            startElement(elementName);
            parseObject();
            endElement(elementName);
        } else if (START_ARRAY.equals(currentToken)) {
            parseArray(elementName);
        } else if (currentToken.isScalarValue()) {
            if (!isEmptyValue()) {
                startElement(elementName);
                parseValue();
                endElement(elementName);
            }
        }
    }

    private boolean isEmptyValue() throws IOException {
        return (jsonParser.getCurrentToken() == VALUE_NULL) || jsonParser.getText().isEmpty();
    }

    private void parseArray(final String elementName) throws IOException, SAXException {
        while (jsonParser.nextToken() != END_ARRAY && jsonParser.getCurrentToken() != null) {
            parseElement(elementName);
        }
    }

    private void parseValue() throws IOException, SAXException {
        if (jsonParser.getCurrentToken() != VALUE_NULL) {
            String text = jsonParser.getText();
            contentHandler.characters(text.toCharArray(), 0, text.length());
        }
    }

    private void startElement(final String elementName) throws SAXException {
        QName qname = ToQName.toQName(root, context, elementName);
        contentHandler.startElement(qname.getNamespaceURI(),
                qname.getLocalPart(), qname.getPrefix() + ":" + qname.getLocalPart(),
                EMPTY_ATTRIBUTES);
    }

    private void endElement(final String elementName) throws SAXException {
        QName qname = ToQName.toQName(root, context, elementName);
        contentHandler.endElement(qname.getNamespaceURI(),
                qname.getLocalPart(), qname.getPrefix() + ":" + qname.getLocalPart());
    }

    private class DocumentLocator implements Locator {

        public String getPublicId() {
            Object sourceRef = jsonParser.getCurrentLocation().getSourceRef();
            if (sourceRef != null) {
                return sourceRef.toString();
            } else {
                return "";
            }
        }

        public String getSystemId() {
            return getPublicId();
        }

        public int getLineNumber() {
            return jsonParser.getCurrentLocation() != null ? jsonParser.getCurrentLocation().getLineNr() : -1;
        }

        public int getColumnNumber() {
            return jsonParser.getCurrentLocation() != null ? jsonParser.getCurrentLocation().getColumnNr() : -1;
        }
    }

    private void writeNamespaceDeclarations(XmlNamespaceContext context) throws SAXException {
        Set<String> keys = new TreeSet(context.getNamespaces().keySet());
        if (root != null && !keys.contains(root.getPrefix())) {
            contentHandler.startPrefixMapping(root.getPrefix(), root.getNamespaceURI());
        }
        for (String prefix : keys) {
            contentHandler.startPrefixMapping(prefix, context.getNamespaceURI(prefix));
        }
    }

}