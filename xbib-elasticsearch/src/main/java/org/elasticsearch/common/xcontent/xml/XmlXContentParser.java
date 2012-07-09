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
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.support.AbstractXContentParser;

public class XmlXContentParser extends AbstractXContentParser implements XContentParser {

    final XmlParser parser;
    
    public XmlXContentParser(XmlParser parser) {
        this.parser = parser;
    }
    
    @Override
    public XContentType contentType() {
        return XContentType.XML;
    }

    @Override
    public Token nextToken() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean doBooleanValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected short doShortValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected int doIntValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected long doLongValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected float doFloatValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected double doDoubleValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void skipChildren() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Token currentToken() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String currentName() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String text() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasTextCharacters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public char[] textCharacters() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int textLength() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int textOffset() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Number numberValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NumberType numberType() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean estimatedNumberType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] binaryValue() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
