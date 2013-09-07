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
package org.xbib.common.xcontent.xml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;
import org.xbib.common.xcontent.support.AbstractXContentParser;

import java.io.IOException;

public class XmlXContentParser extends AbstractXContentParser {

    final JsonParser parser;

    public XmlXContentParser(JsonParser parser) {
        this.parser = parser;
    }


    public XContentType contentType() {
        return XContentType.XML;
    }


    public XContentParser.Token nextToken() throws IOException {
        return convertToken(parser.nextToken());
    }


    public void skipChildren() throws IOException {
        parser.skipChildren();
    }


    public XContentParser.Token currentToken() {
        return convertToken(parser.getCurrentToken());
    }


    public XContentParser.NumberType numberType() throws IOException {
        return convertNumberType(parser.getNumberType());
    }


    public boolean estimatedNumberType() {
        return true;
    }


    public String currentName() throws IOException {
        return parser.getCurrentName();
    }


    protected boolean doBooleanValue() throws IOException {
        return parser.getBooleanValue();
    }


    public String text() throws IOException {
        return parser.getText();
    }


    public boolean hasTextCharacters() {
        return parser.hasTextCharacters();
    }


    public char[] textCharacters() throws IOException {
        return parser.getTextCharacters();
    }


    public int textLength() throws IOException {
        return parser.getTextLength();
    }


    public int textOffset() throws IOException {
        return parser.getTextOffset();
    }


    public Number numberValue() throws IOException {
        return parser.getNumberValue();
    }


    public short doShortValue() throws IOException {
        return parser.getShortValue();
    }


    public int doIntValue() throws IOException {
        return parser.getIntValue();
    }


    public long doLongValue() throws IOException {
        return parser.getLongValue();
    }


    public float doFloatValue() throws IOException {
        return parser.getFloatValue();
    }


    public double doDoubleValue() throws IOException {
        return parser.getDoubleValue();
    }


    public byte[] binaryValue() throws IOException {
        return parser.getBinaryValue();
    }


    public void close() {
        try {
            parser.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private NumberType convertNumberType(JsonParser.NumberType numberType) {
        switch (numberType) {
            case INT:
                return NumberType.INT;
            case LONG:
                return NumberType.LONG;
            case FLOAT:
                return NumberType.FLOAT;
            case DOUBLE:
                return NumberType.DOUBLE;
        }
        throw new IllegalStateException("No matching token for number_type [" + numberType + "]");
    }

    private Token convertToken(JsonToken token) {
        if (token == null) {
            return null;
        }
        switch (token) {
            case FIELD_NAME:
                return Token.FIELD_NAME;
            case VALUE_FALSE:
            case VALUE_TRUE:
                return Token.VALUE_BOOLEAN;
            case VALUE_STRING:
                return Token.VALUE_STRING;
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return Token.VALUE_NUMBER;
            case VALUE_NULL:
                return Token.VALUE_NULL;
            case START_OBJECT:
                return Token.START_OBJECT;
            case END_OBJECT:
                return Token.END_OBJECT;
            case START_ARRAY:
                return Token.START_ARRAY;
            case END_ARRAY:
                return Token.END_ARRAY;
            case VALUE_EMBEDDED_OBJECT:
                return Token.VALUE_EMBEDDED_OBJECT;
        }
        throw new IllegalStateException("No matching token for json_token [" + token + "]");
    }
}