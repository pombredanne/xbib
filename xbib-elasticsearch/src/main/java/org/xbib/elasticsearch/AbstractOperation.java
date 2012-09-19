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
package org.xbib.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.xbib.io.ResultProcessor;
import org.xbib.io.StringData;
import org.xbib.io.operator.ResultOperator;

/**
 * Base class for Elasticsearch operations
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public abstract class AbstractOperation
        implements ResultOperator<ElasticsearchSession, InputStream>, ResultProcessor<InputStream> {

    private final JsonFactory factory = new JsonFactory();
    private IOException exception;
    private String response;
    protected String[] index;
    protected String[] type;
    private ResultProcessor<InputStream> processor;

    public void setIndex(String... index) {
        this.index = index;
    }

    public void setType(String... type) {
        this.type = type;
    }
    
    @Override
    public void setResultProcessor(ResultProcessor<InputStream> processor) {
        this.processor = processor;
    }

    @Override
    public void process(InputStream in) throws IOException {
        JsonParser jp = factory.createJsonParser(in);
        this.response = toString(jp);
    }

    public void processError(InputStream in) throws IOException {
        JsonParser jp = factory.createJsonParser(in);
        toException(jp);
    }

    public String getResponse() {
        return response;
    }

    private String toString(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        // first token must be a START_OBJECT token
        if (token != JsonToken.START_OBJECT) {
            throw new IOException("JSON first token is not START_OBJECT");
        }
        StringBuilder sb = new StringBuilder();
        while (token != null) {
            switch (token) {
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                case VALUE_NULL:
                case VALUE_TRUE:
                case VALUE_FALSE:
                    sb.append(parser.getText());
                    break;
            }
            token = parser.nextToken();
        }
        return sb.toString();
    }

    private void toException(JsonParser parser) throws IOException {
        JsonToken token = parser.nextToken();
        // first token must be a START_OBJECT token
        if (token != JsonToken.START_OBJECT) {
            throw new IOException("JSON first token is not START_OBJECT");
        }
        this.exception = new IOException("unknown");
        while (token != null) {
            switch (token) {
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                case VALUE_NULL:
                case VALUE_TRUE:
                case VALUE_FALSE:
                    // get any StringData value and wrap it into an IOException
                    this.exception = new IOException(parser.getText());
                    break;
            }
            token = parser.nextToken();
        }
        throw exception;
    }

    protected StringData getMapping() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("/org/xbib/elasticsearch/mappings/").append(index[0]);
        if (type != null) {
            sb.append("/").append(type[0]);
        }
        sb.append(".json");
        InputStream in = getClass().getResourceAsStream(sb.toString());
        if (in == null) {
            throw new IOException("mapping file not found: " + sb.toString());
        }
        return new StringData(in);
    }
}
