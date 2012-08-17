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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;

/**
 * Write JSON stream to XML stream. This is realized by transforming
 * Jackson stream events to StaX events. You need a root element to wrap
 * the JSON stream into.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class JsonXmlStreamer {

    //private static final Logger logger = Logger.getLogger(JsonXmlStreamer.class.getName());

    private final static JsonFactory jsonFactory = new JsonFactory();
    private final static XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private NamespaceContext context;
    private QName root;
    private Stack<QName> elements;
    private JsonXmlValueMode mode;

    public JsonXmlStreamer(JsonXmlValueMode mode) {
        this(SimpleNamespaceContext.getInstance(), mode);
    }
    
    public JsonXmlStreamer(NamespaceContext context, JsonXmlValueMode mode) {
        this.context = context;
        this.elements = new Stack();
        this.mode = mode;
    }

    public XMLEventWriter openWriter(OutputStream out) throws XMLStreamException {
        return openWriter(out, "UTF-8");
    }

    public XMLEventWriter openWriter(OutputStream out, String encoding)
            throws XMLStreamException {
        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);
        return outputFactory.createXMLEventWriter(out, encoding);
    }

    public XMLEventWriter openWriter(Writer writer) throws XMLStreamException {
        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);
        return outputFactory.createXMLEventWriter(writer);
    }

    public void writeXMLProcessingInstruction(XMLEventConsumer consumer, String encoding)
            throws XMLStreamException {
        consumer.add(eventFactory.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"" + encoding + "\" standalone=\"no\""));
    }

    public void writeStylesheetInstruction(XMLEventConsumer consumer, String stylesheet)
            throws XMLStreamException {
        consumer.add(eventFactory.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"" + stylesheet + "\""));
    }

    public void toXML(InputStream in, Writer writer, QName root) throws XMLStreamException, IOException {
        toXML(in, openWriter(writer), root);
        writer.flush();
    }

    public void toXML(InputStream in, OutputStream out, QName root)
            throws XMLStreamException, IOException {
        toXML(in, openWriter(out), root);
        out.flush();
    }

    public void toXML(InputStream in, XMLEventConsumer consumer, QName root)
            throws XMLStreamException, IOException {
        this.root = root;
        JsonParser parser = jsonFactory.createJsonParser(new InputStreamReader(in, "UTF-8"));
        JsonToken token = parser.nextToken();
        // first token must be a START_OBJECT token
        if (token != JsonToken.START_OBJECT) {
            throw new IOException("JSON first token is not START_OBJECT");
        }
        QName qname = root;
        boolean namespaceDecls = true;
        try {
            writeXMLProcessingInstruction(consumer, "UTF-8");
            while (token != null) {
                switch (token) {
                    case START_OBJECT:
                        consumer.add(eventFactory.createStartElement(qname, null, null));
                        if (namespaceDecls) {
                            if (!context.getNamespaces().containsKey(qname.getPrefix())) {
                                consumer.add(eventFactory.createNamespace(qname.getPrefix(), qname.getNamespaceURI()));
                            }
                            for (String prefix : context.getNamespaces().keySet()) {
                                String namespaceURI = context.getNamespaceURI(prefix);
                                consumer.add(eventFactory.createNamespace(prefix, namespaceURI));
                            }
                            namespaceDecls = false;
                        }
                        elements.push(qname);
                        break;
                    case END_OBJECT:
                        qname = elements.pop();
                        consumer.add(eventFactory.createEndElement(qname, null));
                        break;
                    case START_ARRAY:
                        elements.push(qname);
                        break;
                    case END_ARRAY:
                        qname = elements.pop();
                        break;
                    case FIELD_NAME:
                        qname = toQName(parser.getCurrentName());
                        break;
                    case VALUE_STRING:
                    case VALUE_NUMBER_INT:
                    case VALUE_NUMBER_FLOAT:
                    case VALUE_NULL:
                    case VALUE_TRUE:
                    case VALUE_FALSE:
                        if (parser.getCurrentName() != null) {
                            qname = toQName(parser.getCurrentName());
                        }
                        String text = parser.getText();
                        int len = text.length();
                        if (len == 0 && mode == JsonXmlValueMode.ERROR_EMPTY_VALUES) {
                            throw new IOException("empty JSON value for " + qname);
                        }
                        if ((len == 0 && mode == JsonXmlValueMode.ALLOW_EMPTY_VALUES) || (len > 0)) {
                            consumer.add(eventFactory.createStartElement(qname, null, null));
                            consumer.add(eventFactory.createCharacters(text));
                            consumer.add(eventFactory.createEndElement(qname, null));
                        }
                        break;
                    default:
                        throw new IOException("unknown JSON token: " + token);
                }
                token = parser.nextToken();
            }
        } catch (JsonParseException e) {
            // Illegal character ((CTRL-CHAR, code 0)): only regular white space (\r, \n, \t) is allowed between tokens
            //logger.log(Level.WARNING, e.getMessage());
        } finally {
            if (consumer instanceof XMLEventWriter) {
                ((XMLEventWriter)consumer).flush();
            }
        }
    }

    private QName toQName(String name) {
        String nsPrefix = root.getPrefix();
        String nsURI = root.getNamespaceURI();
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
        }
        return new QName(nsURI, name, nsPrefix);
    }
}
