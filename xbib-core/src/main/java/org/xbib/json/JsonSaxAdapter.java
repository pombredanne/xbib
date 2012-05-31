/*
 * Copyright 2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xbib.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.namespace.QName;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import static org.codehaus.jackson.JsonToken.END_ARRAY;
import static org.codehaus.jackson.JsonToken.END_OBJECT;
import static org.codehaus.jackson.JsonToken.FIELD_NAME;
import static org.codehaus.jackson.JsonToken.START_ARRAY;
import static org.codehaus.jackson.JsonToken.START_OBJECT;
import static org.codehaus.jackson.JsonToken.VALUE_NULL;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Converts JSON to SAX events.
 *  <pre>
 *  <code>
 *	ContentHandler ch = ...;
 *	JsonSaxAdapter adapter = new JsonSaxAdapter(...);
 *	adapter.parse();
 *  </code>
 *  </pre>
 *  
 */
public class JsonSaxAdapter {

    private static final JsonFactory jsonFactory = new JsonFactory();
    private JsonXmlValueMode mode = JsonXmlValueMode.SKIP_EMPTY_VALUES;
    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
    private final JsonParser jsonParser;
    private final ContentHandler contentHandler;
    private final SimpleNamespaceContext context = SimpleNamespaceContext.getInstance();
    private final QName root;

    public JsonSaxAdapter(final InputSource source, final ContentHandler contentHandler,
            final QName root) throws IOException {
        this(parseJson(source), contentHandler, root);
    }

    public JsonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler,
             final QName root) {
        this.jsonParser = jsonParser;
        this.contentHandler = contentHandler;
        this.root = root;
        contentHandler.setDocumentLocator(new DocumentLocator());
    }

    private static JsonParser parseJson(final InputSource source)
            throws IOException {
        if (source.getByteStream() != null) {
            return jsonFactory.createJsonParser(new InputStreamReader(source.getByteStream(), "UTF-8"));
        } else {
            return jsonFactory.createJsonParser(source.getCharacterStream());
        }
    }

    /**
     * Method parses JSON and emits SAX events.
     */
    public void parse() throws IOException, SAXException {
        jsonParser.nextToken();
        contentHandler.startDocument();
        writeNamespaceDeclarations(context);
        if (root != null)
        contentHandler.startElement(root.getNamespaceURI(), root.getLocalPart(),
                root.getPrefix() + ":" + root.getLocalPart(), EMPTY_ATTRIBUTES);
        parseObject();
        if (root != null)
        contentHandler.endElement(root.getNamespaceURI(), root.getLocalPart(),
                root.getPrefix() + ":" + root.getLocalPart());
        contentHandler.endDocument();
    }

    /**
     * Parses generic object.
     * @return number of elements written
     * @throws IOException
     * @throws JsonParseException
     * @throws Exception
     */
    private void parseObject() throws IOException, SAXException {
        while (jsonParser.nextToken() != null && jsonParser.getCurrentToken() != END_OBJECT) {
            if (FIELD_NAME.equals(jsonParser.getCurrentToken())) {
                String elementName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                parseElement(elementName);
            } else {
                throw new IOException("Error when parsing, expected field name got " 
                        + jsonParser.getCurrentToken());
            }
        }
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
            if (mode != JsonXmlValueMode.SKIP_EMPTY_VALUES || !isEmptyValue()) {
                startElement(elementName);
                parseValue();
                endElement(elementName);
            }
        }
    }

    private void parseArray(final String elementName) throws IOException, SAXException {
        while (jsonParser.nextToken() != END_ARRAY && jsonParser.getCurrentToken() != null) {
            parseElement(elementName);
        }
    }

    private void parseValue() throws IOException, SAXException {
        String text = jsonParser.getText();
        contentHandler.characters(text.toCharArray(), 0, text.length());
    }

    private boolean isEmptyValue() throws IOException {
        return (jsonParser.getCurrentToken() == VALUE_NULL)
                || jsonParser.getText().isEmpty();
    }

    private void startElement(final String elementName) throws SAXException {
        QName qname = toQName(elementName);
        contentHandler.startElement(qname.getNamespaceURI(),
                qname.getLocalPart(), qname.getPrefix() + ":" + qname.getLocalPart(),
                EMPTY_ATTRIBUTES);
    }

    private void endElement(final String elementName) throws SAXException {
        QName qname = toQName(elementName);
        contentHandler.endElement(qname.getNamespaceURI(),
                qname.getLocalPart(), qname.getPrefix() + ":" + qname.getLocalPart());
    }

    private class DocumentLocator implements Locator {

        @Override
        public String getPublicId() {
            Object sourceRef = jsonParser.getCurrentLocation().getSourceRef();
            if (sourceRef != null) {
                return sourceRef.toString();
            } else {
                return "";
            }
        }

        @Override
        public String getSystemId() {
            return getPublicId();
        }

        @Override
        public int getLineNumber() {
            return jsonParser.getCurrentLocation() != null ? jsonParser.getCurrentLocation().getLineNr() : -1;
        }

        @Override
        public int getColumnNumber() {
            return jsonParser.getCurrentLocation() != null ? jsonParser.getCurrentLocation().getColumnNr() : -1;
        }
    }

    private void writeNamespaceDeclarations(NamespaceContext context) throws SAXException {
        Set<String> keys = new TreeSet(context.getNamespaceMap().keySet());
        if (root != null && !keys.contains(root.getPrefix())) {
            contentHandler.startPrefixMapping(root.getPrefix(), root.getNamespaceURI());
        }
        for (String prefix : keys) {
            contentHandler.startPrefixMapping(prefix, context.getNamespaceURI(prefix));
        }
    }

    private QName toQName(String name) {
        String nsPrefix;
        String nsURI;
        // convert all JSON names beginning with an underscore to elements in default namespace
        if (name.startsWith("_")) {
            name = name.substring(1);
        }
        int pos = name.indexOf(':');
        if (pos > 0) {
            // check for configured namespace
            nsPrefix = name.substring(0, pos);
            nsURI = context.getNamespaceURI(nsPrefix);
            if (nsURI == null) {
                throw new IllegalArgumentException("unknown namespace prefix: " + nsPrefix);
            }
            name = name.substring(pos + 1);
        } else if (root != null) {
            nsPrefix = root.getPrefix();
            nsURI = root.getNamespaceURI();
        } else {
            nsPrefix = "";
            nsURI = "";
        }
        return new QName(nsURI, name, nsPrefix);
    }
}