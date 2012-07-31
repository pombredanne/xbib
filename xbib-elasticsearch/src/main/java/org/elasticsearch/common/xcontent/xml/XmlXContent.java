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
import java.io.Reader;
import java.io.Writer;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentGenerator;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.xml.sax.ContentHandler;

public class XmlXContent implements XContent {
    
    public static XContentBuilder contentBuilder() throws IOException {
        return XContentBuilder.builder(xmlXContent);
    }    
    
    public static XContentBuilder contentBuilder(ContentHandler handler) throws IOException {
        return XContentBuilder.builder(new XmlXContent().handler(handler));
    }
    
    public final static XmlXContent xmlXContent = new XmlXContent();
    
    private ContentHandler handler;
    
    private XmlXContent() {
    }
    
    public XmlXContent handler(ContentHandler handler) {
        this.handler = handler;
        return this;
    }
    
    @Override
    public XContentType type() {
        return XContentType.XML;
    }

    @Override
    public byte streamSeparator() {
        return '\n';
    }
    
    @Override
    public XContentGenerator createGenerator(OutputStream os) throws IOException {
        return new XmlXContentGenerator(new XmlGenerator(os, handler));
    }

    @Override
    public XContentGenerator createGenerator(Writer writer) throws IOException {
        return new XmlXContentGenerator(new XmlGenerator(writer, handler));
    }

    @Override
    public XContentParser createParser(String content) throws IOException {
        return new XmlXContentParser(new XmlParser(content, handler));
    }

    @Override
    public XContentParser createParser(InputStream is) throws IOException {
        return new XmlXContentParser(new XmlParser(is, handler));
    }

    @Override
    public XContentParser createParser(byte[] data) throws IOException {
        return new XmlXContentParser(new XmlParser(data, handler));
    }

    @Override
    public XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        return new XmlXContentParser(new XmlParser(data, offset, length, handler));
    }

    @Override
    public XContentParser createParser(Reader reader) throws IOException {
        return new XmlXContentParser(new XmlParser(reader, handler));
    }

/**
 * ES 0.20 method
 * 
 * @Override
    public XContentParser createParser(BytesHolder bytes) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
   */ 
}
