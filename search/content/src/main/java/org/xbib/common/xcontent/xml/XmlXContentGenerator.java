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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import org.xbib.common.bytes.BytesReference;
import org.xbib.common.xcontent.XContentType;
import org.xbib.common.xcontent.json.JsonXContentGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class XmlXContentGenerator extends JsonXContentGenerator {

    public XmlXContentGenerator(JsonGenerator generator) {
        super(generator);
    }

    
    public XContentType contentType() {
        return XContentType.XML;
    }

    
    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        JsonParser parser = XmlXContent.xmlFactory.createJsonParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }


    public void writeRawField(String fieldName, byte[] content, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        JsonParser parser = XmlXContent.xmlFactory.createJsonParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }


    public void writeRawField(String fieldName, BytesReference content, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        JsonParser parser;
        if (content.hasArray()) {
            parser = XmlXContent.xmlFactory.createJsonParser(content.array(), content.arrayOffset(), content.length());
        } else {
            parser = XmlXContent.xmlFactory.createJsonParser(content.streamInput());
        }
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }


    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        JsonParser parser = XmlXContent.xmlFactory.createJsonParser(content, offset, length);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }
}
