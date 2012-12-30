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

package org.xbib.common.xcontent.support;

import org.xbib.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public abstract class AbstractXContentParser implements XContentParser {

    
    public boolean booleanValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_NUMBER) {
            return intValue() != 0;
        } else if (token == Token.VALUE_STRING) {
            String s = new String(textCharacters(), textOffset(), textLength());
            return Boolean.parseBoolean(s);
        }
        return doBooleanValue();
    }

    protected abstract boolean doBooleanValue() throws IOException;

    
    public short shortValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Short.parseShort(text());
        }
        return doShortValue();
    }

    protected abstract short doShortValue() throws IOException;

    
    public int intValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Integer.parseInt(text());
        }
        return doIntValue();
    }

    protected abstract int doIntValue() throws IOException;

    
    public long longValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Long.parseLong(text());
        }
        return doLongValue();
    }

    protected abstract long doLongValue() throws IOException;

    
    public float floatValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Float.parseFloat(text());
        }
        return doFloatValue();
    }

    protected abstract float doFloatValue() throws IOException;

    
    public double doubleValue() throws IOException {
        Token token = currentToken();
        if (token == Token.VALUE_STRING) {
            return Double.parseDouble(text());
        }
        return doDoubleValue();
    }

    protected abstract double doDoubleValue() throws IOException;

    
    public String textOrNull() throws IOException {
        if (currentToken() == Token.VALUE_NULL) {
            return null;
        }
        return text();
    }

    
    public Map<String, Object> map() throws IOException {
        return XContentMapConverter.readMap(this);
    }

    
    public Map<String, Object> mapOrdered() throws IOException {
        return XContentMapConverter.readOrderedMap(this);
    }

    
    public Map<String, Object> mapAndClose() throws IOException {
        try {
            return map();
        } finally {
            close();
        }
    }

    
    public Map<String, Object> mapOrderedAndClose() throws IOException {
        try {
            return mapOrdered();
        } finally {
            close();
        }
    }
}
