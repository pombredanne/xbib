/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.xcontent.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.elasticsearch.common.xcontent.XContentParser.NumberType;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.common.xcontent.xml.namespace.ES;
import org.xml.sax.ContentHandler;

public class XmlParser {

    private final static QName DEFAULT_QNAME = new QName(ES.NS_URI, "result", ES.NS_PREFIX);
    
    private XMLInputFactory factory = XMLInputFactory.newInstance();    
    
    private final Reader reader;
    
    XMLEventReader xmlReader;
    
    private ContentHandler handler;
    
    private Stack<QName> elements;
    
    private Token token;

    public XmlParser(byte[] data, ContentHandler handler) throws IOException {
        this(new String(data), handler);
    }
    
    public XmlParser(byte[] data, int offset, int length, ContentHandler handler) throws IOException {
        this(new String(data, offset, length), handler);
    }

    public XmlParser(String content, ContentHandler handler) throws IOException {
        this(new StringReader(content), handler);
    }

    public XmlParser(InputStream is, ContentHandler handler) throws UnsupportedEncodingException, IOException {
        this(new InputStreamReader(is, "UTF-8"), handler);
    }
    
    public XmlParser(Reader reader, ContentHandler handler) throws IOException {
        this.reader = reader;
        this.handler = handler;
        this.elements = new Stack();
        this.token = null;
        try {
            xmlReader = factory.createXMLEventReader(reader);
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }
    
    public Token nextToken() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected boolean doBooleanValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected short doShortValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected int doIntValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected long doLongValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected float doFloatValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected double doDoubleValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void skipChildren() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Token currentToken() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String currentName() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String text() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasTextCharacters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public char[] textCharacters() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int textLength() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int textOffset() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Number numberValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NumberType numberType() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean estimatedNumberType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public byte[] binaryValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
