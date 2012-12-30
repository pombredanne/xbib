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

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.xbib.elements.scripting.ScriptElement;
import org.xbib.io.InputStreamService;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class ElementMapFactory<K> {

    private final static Logger logger = LoggerFactory.getLogger(ElementMapFactory.class.getName());
    private final static Map<String, Map<String, Element>> maps = new HashMap();

    private ElementMapFactory() {
    }

    public synchronized static Map<String, Element> getElementMap(String format)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        if (!maps.containsKey(format)) {
            init("", format);
        }
        return maps.get(format);
    }

    public synchronized static Map<String, Element> getElementMap(String path, String format)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        if (!maps.containsKey(format)) {
            init(path, format);
        }
        return maps.get(format);
    }

    private static void init(String path, String format)
            throws IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, NoSuchMethodException, IllegalArgumentException,
            InvocationTargetException {
        InputStream json = ElementMapFactory.class.getResourceAsStream(path + format + ".json");
        if (json == null) {
            throw new IOException("format " + format + " not found: " + path + format + ".json");
        }
        final HashMap<String, Element> map = new HashMap();
        HashMap<String, Map<String, Object>> result = new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true).readValue(json, HashMap.class);
        for (String k : result.keySet()) {
            Map<String, Object> struct = result.get(k);
            Element element = null;
            String clazzName = getPackageFromPath(path + format) + k;
            Collection<String> values = (Collection<String>) struct.get("values");
            String type = (String) struct.get("type");
            try {
                if (type != null && type.startsWith("application/x-")) {
                    String name = type.substring("application/x-".length());
                    String script = (String) struct.get("script");
                    String invocable = (String) struct.get("class");
                    String source = (String) struct.get("source");
                    if (script != null) {
                        InputStream input = ElementMapFactory.class.getResourceAsStream(path + script);
                        if (input == null) {
                            throw new IOException(path + script + " not found: " + path + script);
                        }
                        InputStreamReader reader = new InputStreamReader(input, "UTF-8");
                        ScriptElement scriptElement = new ScriptElement(name, InputStreamService.getString(reader), invocable);
                        scriptElement.setSettings(struct);
                        element = scriptElement.getElement();
                        reader.close();
                    } else if (source != null) {
                        ScriptElement scriptElement = new ScriptElement(name, source, invocable);
                        scriptElement.setSettings(struct);
                        element = scriptElement.getElement();
                    }
                } else {
                    // Java            
                    Class clazz = Class.forName(clazzName);
                    Method factoryMethod = clazz.getDeclaredMethod("getInstance");
                    Object singleton = factoryMethod.invoke(null);
                    element = (Element) singleton;
                    // set settings
                    element.setSettings(struct);
                }
            } catch (ClassNotFoundException e) {
                logger.warn("missing class: " + e.getMessage());
            }
            // connect each value to an element class
            for (String value : values) {
                if (!map.containsKey(value)) {
                    map.put(value, element);
                } else {
                    throw new IOException("value already exists: " + value);
                }
            }
        }
        maps.put(format, map);
    }

    private static String getPackageFromPath(String path) {
        // some hackery ahead
        String packageName = path.replace('/', '.');
        if (packageName.charAt(0) == '.') {
            packageName = packageName.substring(1);
        }
        if (packageName.charAt(packageName.length() - 1) != '.') {
            packageName = packageName + '.';
        }
        return packageName;
    }
}
