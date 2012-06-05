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
import org.elasticsearch.common.Base64;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentString;
import org.elasticsearch.common.xcontent.XContentType;

public class XmlXContentGenerator implements XContentGenerator {

    protected final XmlGenerator generator;

    public XmlXContentGenerator(XmlGenerator generator) {
        this.generator = generator;
    }

    @Override
    public XContentType contentType() {
        return XContentType.XML;
    }

    @Override
    public void usePrettyPrint() {
        generator.usePrettyPrint();
    }

    @Override
    public void writeStartArray() throws IOException {
        generator.writeStartArray();
    }

    @Override
    public void writeEndArray() throws IOException {
        generator.writeEndArray();
    }

    @Override
    public void writeStartObject() throws IOException {
        generator.writeStartObject();
    }

    @Override
    public void writeEndObject() throws IOException {
        generator.writeEndObject();
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        generator.writeFieldName(name);
    }

    @Override
    public void writeFieldName(XContentString name) throws IOException {
        writeFieldName(name.getValue());
    }

    @Override
    public void writeString(String text) throws IOException {
        generator.writeString(text);
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        writeString(new String(text, offset, len));
    }

    @Override
    public void writeBinary(byte[] data, int offset, int len) throws IOException {
        writeString(Base64.encodeBytes(data, offset, len));
    }

    @Override
    public void writeBinary(byte[] data) throws IOException {
        writeString(Base64.encodeBytes(data));
    }

    @Override
    public void writeNumber(int v) throws IOException {
        writeString(Integer.toString(v));
    }

    @Override
    public void writeNumber(long v) throws IOException {
        writeString(Long.toString(v));
    }

    @Override
    public void writeNumber(double d) throws IOException {
        writeString(Double.toString(d));
    }

    @Override
    public void writeNumber(float f) throws IOException {
        writeString(Float.toString(f));
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        writeString(Boolean.toString(b));
    }

    @Override
    public void writeNull() throws IOException {
        writeString("null");
    }

    @Override
    public void writeStringField(String fieldName, String value) throws IOException {
        writeFieldName(fieldName);
        writeString(value);
    }

    @Override
    public void writeStringField(XContentString fieldName, String value) throws IOException {
        writeFieldName(fieldName);
        writeString(value);
    }

    @Override
    public void writeBooleanField(String fieldName, boolean value) throws IOException {
        writeFieldName(fieldName);
        writeBoolean(value);
    }

    @Override
    public void writeBooleanField(XContentString fieldName, boolean value) throws IOException {
        writeFieldName(fieldName);
        writeBoolean(value);
    }

    @Override
    public void writeNullField(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeNull();
    }

    @Override
    public void writeNullField(XContentString fieldName) throws IOException {
        writeFieldName(fieldName);
        writeNull();
    }

    @Override
    public void writeNumberField(String fieldName, int value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, int value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, long value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, long value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, double value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, double value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    @Override
    public void writeNumberField(String fieldName, float value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    @Override
    public void writeNumberField(XContentString fieldName, float value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    @Override
    public void writeBinaryField(String fieldName, byte[] data) throws IOException {
        writeFieldName(fieldName);
        writeBinary(data);
    }

    @Override
    public void writeBinaryField(XContentString fieldName, byte[] data) throws IOException {
        writeFieldName(fieldName);
        writeBinary(data);
    }

    @Override
    public void writeArrayFieldStart(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartArray();
    }

    @Override
    public void writeArrayFieldStart(XContentString fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartArray();
    }

    @Override
    public void writeObjectFieldStart(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartObject();
    }

    @Override
    public void writeObjectFieldStart(XContentString fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartObject();
    }

    @Override
    public void writeRawField(String fieldName, byte[] content, OutputStream out) throws IOException {
        generator.writeFieldName(fieldName);
        flush();
        out.write(content);
    }

    @Override
    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream out) throws IOException {
        generator.writeFieldName(fieldName);
        flush();
        out.write(content, offset, length);
    }

    @Override
    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        generator.writeFieldName(fieldName);
        flush();
        Streams.copy(content, bos);
    }

    @Override
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

    @Override
    public void flush() throws IOException {
        generator.flush();
    }

    @Override
    public void close() throws IOException {
        generator.close();
    }
}
