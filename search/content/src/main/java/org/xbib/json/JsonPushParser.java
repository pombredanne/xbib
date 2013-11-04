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
package org.xbib.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.xbib.common.io.FastByteArrayOutputStream;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

public class JsonPushParser extends FastByteArrayOutputStream {

    private static final Logger logger = LoggerFactory.getLogger(JsonPushParser.class.getName());

    private static final JsonFactory factory = new JsonFactory();

    private List<JsonConsumer> consumers = newLinkedList();

    private JsonParser jsonParser;

    public JsonPushParser register(JsonConsumer consumer) {
        consumers.add(consumer);
        return this;
    }

    public void parse() throws IOException {
        logger.info("parsing buf = {}", new String(buf));
        //if (jsonParser == null) {
            jsonParser = factory.createParser(buf);
        //}
        JsonToken token;
        try {
            token = jsonParser.nextToken();
            while (token != null) {
                for (JsonConsumer consumer : consumers) {
                    consumer.add(token);
                }
                token = jsonParser.nextToken();
            }
        } catch (JsonParseException e) {
            JsonLocation location = jsonParser.getTokenLocation();
            logger.info("token location={} {} ", location, location.getCharOffset());
            int off = (int)location.getCharOffset();
            if (off >= 0 && off <= buf.length) {
                reset();
                write(buf, off, buf.length-off-1);
            }
        }
    }

}
