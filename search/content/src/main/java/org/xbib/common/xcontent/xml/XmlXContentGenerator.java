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

package org.xbib.common.xcontent.xml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import org.xbib.common.bytes.BytesReference;
import org.xbib.common.xcontent.XContentGenerator;
import org.xbib.common.xcontent.XContentHelper;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.xml.ToQName;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * Content generator for XML formatted content
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class XmlXContentGenerator implements XContentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(XmlXContentGenerator.class.getName());

    protected final ToXmlGenerator generator;

    private XmlXParams params = XmlXParams.getDefaultParams();

    private boolean rootUsed = false;

    public XmlXContentGenerator(ToXmlGenerator generator) {
        this.generator = generator;
        generator.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
    }

    public XmlXContentGenerator setParams(XmlXParams params) {
        this.params = params;
        try {
            generator.getStaxWriter().setDefaultNamespace(params.getQName().getNamespaceURI());
        } catch(XMLStreamException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    public XmlXParams getParams() {
        return params;
    }

    public XmlNamespaceContext getNamespaceContext() {
        return params.getNamespaceContext();
    }

    public XContentType contentType() {
        return XContentType.XML;
    }

    public void usePrettyPrint() {
        generator.useDefaultPrettyPrinter();
    }


    public void writeStartArray() throws IOException {
        generator.writeStartArray();
    }


    public void writeEndArray() throws IOException {
        generator.writeEndArray();
    }

    public void writeStartObject() throws IOException {
        if (!rootUsed) {
            generator.startWrappedValue(null, getParams().getQName());
        }
        generator.writeStartObject();
        if (!rootUsed) {
            try {
                for (String prefix : getNamespaceContext().getNamespaces().keySet()) {
                    generator.getStaxWriter().writeNamespace(prefix, getNamespaceContext().getNamespaceURI(prefix));
                }
            } catch (XMLStreamException e) {
                logger.error(e.getMessage(), e);
            }
            rootUsed = true;
        }
    }

    public void writeEndObject() throws IOException {
        generator.writeEndObject();
    }

    public void writeFieldName(String name) throws IOException {
        writeFieldNameWithNamespace(name);
    }

    public void writeString(String text) throws IOException {
        generator.writeString(text);
    }

    public void writeString(char[] text, int offset, int len) throws IOException {
        generator.writeString(text, offset, len);
    }

    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        generator.writeUTF8String(text, offset, length);
    }

    public void writeBinary(byte[] data, int offset, int len) throws IOException {
        generator.writeBinary(data, offset, len);
    }

    public void writeBinary(byte[] data) throws IOException {
        generator.writeBinary(data);
    }

    public void writeNumber(int v) throws IOException {
        generator.writeNumber(v);
    }

    public void writeNumber(long v) throws IOException {
        generator.writeNumber(v);
    }

    public void writeNumber(double d) throws IOException {
        generator.writeNumber(d);
    }

    public void writeNumber(float f) throws IOException {
        generator.writeNumber(f);
    }

    public void writeBoolean(boolean state) throws IOException {
        generator.writeBoolean(state);
    }

    public void writeNull() throws IOException {
        generator.writeNull();
    }

    public void writeStringField(String fieldName, String value) throws IOException {
        generator.writeStringField(fieldName, value);
    }

    public void writeBooleanField(String fieldName, boolean value) throws IOException {
        generator.writeBooleanField(fieldName, value);
    }

    public void writeNullField(String fieldName) throws IOException {
        generator.writeNullField(fieldName);
    }

    public void writeNumberField(String fieldName, int value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    public void writeNumberField(String fieldName, long value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    public void writeNumberField(String fieldName, double value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    public void writeNumberField(String fieldName, float value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    public void writeBinaryField(String fieldName, byte[] data) throws IOException {
        generator.writeBinaryField(fieldName, data);
    }

    public void writeArrayFieldStart(String fieldName) throws IOException {
        generator.writeArrayFieldStart(fieldName);
    }

    public void writeObjectFieldStart(String fieldName) throws IOException {
        generator.writeObjectFieldStart(fieldName);
    }

    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        writeFieldNameWithNamespace(fieldName);
        JsonParser parser = XmlXContent.xmlFactory().createParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    public void writeRawField(String fieldName, byte[] content, OutputStream bos) throws IOException {
        writeFieldNameWithNamespace(fieldName);
        JsonParser parser = XmlXContent.xmlFactory().createParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    public void writeRawField(String fieldName, BytesReference content, OutputStream bos) throws IOException {
        writeFieldNameWithNamespace(fieldName);
        JsonParser parser;
        if (content.hasArray()) {
            parser = XmlXContent.xmlFactory().createParser(content.array(), content.arrayOffset(), content.length());
        } else {
            parser = XmlXContent.xmlFactory().createParser(content.streamInput());
        }
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException {
        writeFieldNameWithNamespace(fieldName);
        JsonParser parser = XmlXContent.xmlFactory().createParser(content, offset, length);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    public void copyCurrentStructure(XContentParser parser) throws IOException {
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser instanceof XmlXContentParser) {
            generator.copyCurrentStructure(((XmlXContentParser) parser).parser);
        } else {
            XContentHelper.copyCurrentStructure(this, parser);
        }
    }

    public void flush() throws IOException {
        generator.flush();
    }


    public void close() throws IOException {
        generator.close();
    }

    private void writeFieldNameWithNamespace(String name) throws IOException {
        QName qname = ToQName.toQName(params.getQName(), params.getNamespaceContext(), name);
        generator.setNextName(qname);
        generator.writeFieldName(qname.getLocalPart());
    }

}
