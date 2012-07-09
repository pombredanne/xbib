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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Stack;
import javax.xml.namespace.QName;
import org.elasticsearch.common.xcontent.xml.namespace.ES;
import org.elasticsearch.common.xcontent.xml.namespace.NamespaceContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class XmlGenerator {

    private final static QName DEFAULT_QNAME = new QName(ES.NS_URI, "result", ES.NS_PREFIX);
    private Writer writer;
    private final QName root;
    private final NamespaceContext context;
    private final ContentHandler handler;
    private QName qname;
    private Stack<QName> objects = new Stack();
    private StringBuilder sb;
    private boolean namespaceDecl = false;
    private boolean closed = false;

    public XmlGenerator(OutputStream out)
            throws IOException {
        this(null, new OutputStreamWriter(out, "UTF-8"), NamespaceContext.getInstance(), null);
    }

    public XmlGenerator(OutputStream out, ContentHandler handler)
            throws IOException {
        this(null, new OutputStreamWriter(out, "UTF-8"), NamespaceContext.getInstance(), handler);
    }

    public XmlGenerator(Writer writer)
            throws IOException {
        this(null, writer, NamespaceContext.getInstance(), null);
    }

    public XmlGenerator(Writer writer, ContentHandler handler)
            throws IOException {
        this(null, writer, NamespaceContext.getInstance(), handler);
    }

    public XmlGenerator(QName root, OutputStream out)
            throws IOException {
        this(root, new OutputStreamWriter(out, "UTF-8"), NamespaceContext.getInstance(), null);
    }

    public XmlGenerator(QName root, Writer writer)
            throws IOException {
        this(root, writer, NamespaceContext.getInstance(), null);
    }

    public XmlGenerator(QName root, Writer writer, NamespaceContext context, ContentHandler handler)
            throws IOException {
        this.root = root == null ? DEFAULT_QNAME : root;
        this.qname = this.root;
        this.context = context;
        this.objects = new Stack();
        this.sb = new StringBuilder();
        this.writer = writer;
        this.handler = handler;
        if (handler != null) {
            try {
                handler.startDocument();
            } catch (SAXException ex) {
                throw new IOException(ex);
            }
        }
    }

    public void usePrettyPrint() {
        // not implemented
    }

    public void writeStartObject() throws IOException {
        objects.push(qname);
        StringBuilder attrs = new StringBuilder();
        if (namespaceDecl) {
            for (String prefix : context.getNamespaces().keySet()) {
                String namespaceURI = context.getNamespaceURI(prefix);
                attrs.append(" xmlns:").append(prefix).append("=\"").append(namespaceURI).append("\"");
                try {
                    if (handler != null) {
                        handler.startPrefixMapping(prefix, namespaceURI);
                    }
                } catch (SAXException ex) {
                    throw new IOException(ex);
                }
            }
            namespaceDecl = false;
        }
        beginElement(qname.getPrefix(), qname.getLocalPart(), attrs);
        if (handler != null) {
            try {
                handler.startElement(qname.getNamespaceURI(), qname.getLocalPart(), qname.getPrefix() + ":" + qname.getLocalPart(), null);
            } catch (SAXException ex) {
                throw new IOException(ex);
            }
        }
        flush();
    }

    public void writeEndObject() throws IOException {
        qname = objects.pop();
        endElement(qname.getPrefix(), qname.getLocalPart());
        try {
            if (handler != null) {
                handler.endElement(qname.getNamespaceURI(), qname.getLocalPart(), qname.getPrefix() + ":" + qname.getLocalPart());
            }
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        flush();
    }

    public void writeStartArray() throws IOException {
    }

    public void writeEndArray() throws IOException {
    }

    public void writeFieldName(String name) throws IOException {
        qname = toQName(name);
    }

    public void writeString(String text) throws IOException {
        beginElement(qname.getPrefix(), qname.getLocalPart());
        text(text);
        endElement(qname.getPrefix(), qname.getLocalPart());
        try {
            if (handler != null) {
                handler.startElement(qname.getNamespaceURI(), qname.getLocalPart(), qname.getPrefix() + ":" + qname.getLocalPart(), null);
                handler.characters(text.toCharArray(), 0, text.length());
                handler.endElement(qname.getNamespaceURI(), qname.getLocalPart(), qname.getPrefix() + ":" + qname.getLocalPart());
            }
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        flush();
    }

    public void writeRawField(String fieldName, byte[] content, OutputStream out) throws IOException {
        /*
         * qname = toQName(fieldName); beginElement(qname.getPrefix(),
         * qname.getLocalPart()); text(XMLUtil.escape(new String(content)));
         * endElement(qname.getPrefix(), qname.getLocalPart());
         * UnicodeUtil.UTF8Result result =
         * Unicode.unsafeFromStringAsUtf8(sb.toString());
         * out.write(result.result);8/ }
         *
         * public void writeRawField(String fieldName, byte[] content, int
         * offset, int length, OutputStream out) throws IOException { /*qname =
         * toQName(fieldName); beginElement(qname.getPrefix(),
         * qname.getLocalPart()); text(XMLUtil.escape(new String(content,
         * offset, length))); endElement(qname.getPrefix(),
         * qname.getLocalPart()); UnicodeUtil.UTF8Result result =
         * Unicode.unsafeFromStringAsUtf8(sb.toString());
        out.write(result.result);
         */
    }

    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void copyCurrentStructure(XmlParser parser) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void flush() throws IOException {
        writer.write(sb.toString());
        writer.flush();
        sb.setLength(0);
    }

    public void close() throws IOException {
        if (closed) return;
        closed = true;
        writer.close();
        sb = null;
        if (handler != null) {
            try {
                handler.endDocument();
            } catch (SAXException ex) {
                throw new IOException(ex);
            }
        }
    }

    /**
     * Convert abbreviated or non-abbreviaed XML name to a qualified name.
     * @param name
     * @return 
     */
    private QName toQName(String name) {
        String nsPrefix = root.getPrefix();
        String nsURI = root.getNamespaceURI();
        if (name.startsWith("_")) {
            name = name.substring(1);
        }
        int pos = name.indexOf(':');
        if (pos > 0) {
            nsPrefix = name.substring(0, pos);
            nsURI = context.getNamespaceURI(nsPrefix);
            if (nsURI == null) {
                throw new IllegalArgumentException("unknown namespace prefix: " + nsPrefix);
            }
            name = name.substring(pos + 1);
        }
        return new QName(nsURI, name, nsPrefix);
    }

    /**
     * Write begin of XML markup
     * @param prefix
     * @param localPart 
     */
    private void beginElement(String prefix, String localPart) {
        sb.append('<').append(prefix).append(':').append(localPart).append('>');
    }

    /**
     * Write begin of XML markup element with attributes
     * @param prefix
     * @param localPart
     * @param attrs 
     */
    private void beginElement(String prefix, String localPart, StringBuilder attrs) {
        sb.append('<').append(prefix).append(':').append(localPart).append(attrs).append('>');
    }

    /**
     * Write end of XML markup element
     * @param prefix
     * @param localPart 
     */
    private void endElement(String prefix, String localPart) {
        sb.append("</").append(prefix).append(':').append(localPart).append('>');
    }

    /**
     * Write XML text. The text is escaped with XML escapes.
     * @param text 
     */
    private void text(CharSequence text) {
        escape(sb, text);
    }
    
    /**
     * Replace special characters with XML escapes:
     * <pre>
     * &amp; <small>(ampersand)</small> is replaced by &amp;amp;
     *
     * &lt; <small>(less than)</small> is replaced by &amp;lt;
     * &gt; <small>(greater than)</small> is replaced by &amp;gt;
     * &quot; <small>(double quote)</small> is replaced by &amp;quot;
     * </pre>
     *
     * @param string The string to be escaped.
     * @return The escaped string.
     */
    public static void escape(StringBuilder sb, CharSequence string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
    }
}
