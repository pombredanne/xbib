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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;

public class ElementMapFactory {

    private final static String ROOT_PATH = "/org/xbib/elements/";
    
    private final static Map<String, Map<String, Element>> maps = new HashMap();

    private ElementMapFactory() {
    }

    public synchronized static Map<String, Element> getElementMap(String format)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        if (!maps.containsKey(format)) {
            init(format);
        }
        return maps.get(format);
    }

    private static void init(String format)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        InputStream json = ElementMapFactory.class.getResourceAsStream(ROOT_PATH + format + ".json");
        if (json == null) {
            throw new IOException("format " + format + " not found");
        }
        final GroovyClassLoader gcl = new GroovyClassLoader();
        final HashMap<String, Element> map = new HashMap();
        HashMap<String, Map<String, Object>> result = new ObjectMapper().readValue(json, HashMap.class);
        for (String k : result.keySet()) {
            Map<String, Object> struct = result.get(k);
            Element element;
            String clazzName = "org.xbib.elements." + format + "." + k;
            Collection<String> values= (Collection<String>) struct.get("values");
            if ("application/x-groovy".equals(struct.get("type"))) {
                String script = (String)struct.get("script");
                String source = (String)struct.get("source");
                element = null;
                if (script != null) {
                    InputStream groovy = ElementMapFactory.class.getResourceAsStream(ROOT_PATH + script);
                    if (groovy == null) throw new IOException(ROOT_PATH + script + " not found");
                    InputStreamReader reader = new InputStreamReader(groovy, "UTF-8");
                    GroovyCodeSource gcs = new GroovyCodeSource(reader, "class", ROOT_PATH);
                    Class clazz = gcl.parseClass(gcs);
                    Object singleton = clazz.newInstance();
                    element = (Element) singleton;
                } else if (source != null) {
                    Class clazz = gcl.parseClass(source);
                    Object singleton = clazz.newInstance();
                    element = (Element) singleton;
                }
                element.setParameter(struct);
            } else {
                Class clazz = Class.forName(clazzName);
                Method factoryMethod = clazz.getDeclaredMethod("getInstance");
                Object singleton = factoryMethod.invoke(null);
                element = (Element) singleton;
            }
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
}
