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
package org.xbib.elements;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class ValueMapFactory {

    private final static Logger logger = LoggerFactory.getLogger(ValueMapFactory.class.getName());
    private final static Map<String, Object> maps = new HashMap();

    private ValueMapFactory() {
    }

    public static Map getMap(String format) {
        return getMap("", format);
    }

    public synchronized static Map getMap(String path, String format) {
        if (!maps.containsKey(format)) {
            try {
                InputStream json = ValueMapFactory.class.getResourceAsStream(path + format + ".json");
                if (json == null) {
                    throw new IOException("format " + format + " not found: " + path + format + ".json");
                }
                ObjectMapper mapper = new ObjectMapper();
                maps.put(format, mapper.readValue(json, HashMap.class));
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return (Map) maps.get(format);
    }

    public static Map<String, String> getAssocStringMap(String format) {
        return getAssocStringMap("", format);
    }

    public synchronized static Map<String, String> getAssocStringMap(String path, String format) {
        if (!maps.containsKey(format)) {
            try {
                InputStream json = ValueMapFactory.class.getResourceAsStream(path + format + ".json");
                if (json == null) {
                    throw new IOException("format " + format + " not found: " + path + format + ".json");
                }
                Map result = new ObjectMapper().readValue(json, HashMap.class);
                Object values = result.get(format);
                Collection<String> c = (Collection<String>) values;
                if (c != null) {
                    // assoc map
                    final Map<String, String> map = new HashMap();
                    Iterator<String> it = c.iterator();
                    for (int i = 0; i < c.size(); i += 2) {
                        map.put(it.next(), it.next());
                    }
                    maps.put(format, map);
                }
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return (Map<String, String>) maps.get(format);
    }
}
