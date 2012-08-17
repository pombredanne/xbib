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
import javax.script.ScriptException;
import org.xbib.elements.scripting.ScriptElement;
import org.xbib.io.InputStreamService;

public class ElementMapFactory<K> {

    private final static String ROOT_PATH = "/org/xbib/elements/";
    private final static Map<String, Map<String, Element>> maps = new HashMap();

    private ElementMapFactory() {
    }

    public synchronized static Map<String, Element> getElementMap(String format)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, 
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ScriptException {
        if (!maps.containsKey(format)) {
            init(ROOT_PATH, format);
        }
        return maps.get(format);
    }

    public synchronized static Map<String, Element> getElementMap(String path, String format)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, 
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ScriptException {
        if (!maps.containsKey(format)) {
            init(path, format);
        }
        return maps.get(format);
    }

    private static void init(String path, String format)
            throws IOException, ClassNotFoundException, InstantiationException, 
            IllegalAccessException, NoSuchMethodException, IllegalArgumentException, 
            InvocationTargetException, ScriptException {
        InputStream json = ElementMapFactory.class.getResourceAsStream(path + format + ".json");
        if (json == null) {
            throw new IOException("format " + format + " not found");
        }
        final GroovyClassLoader gcl = new GroovyClassLoader();
        final HashMap<String, Element> map = new HashMap();
        HashMap<String, Map<String, Object>> result = new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true).readValue(json, HashMap.class);
        for (String k : result.keySet()) {
            Map<String, Object> struct = result.get(k);
            Element element;
            String clazzName = getPackageFromPath(path + format) + k;
            Collection<String> values = (Collection<String>) struct.get("values");
            String type = (String) struct.get("type");
            if ("application/x-groovy".equals(type)) {
                String script = (String) struct.get("script");
                String source = (String) struct.get("source");
                element = null;
                if (script != null) {
                    // groovy script
                    InputStream input = ElementMapFactory.class.getResourceAsStream(path + script);
                    if (input == null) {
                        throw new IOException(path + script + " not found");
                    }
                    InputStreamReader reader = new InputStreamReader(input, "UTF-8");
                    GroovyCodeSource gcs = new GroovyCodeSource(reader, "class", ROOT_PATH);
                    Class clazz = gcl.parseClass(gcs);
                    Object singleton = clazz.newInstance();
                    element = (Element) singleton;
                } else if (source != null) {
                    // external groovy source
                    Class clazz = gcl.parseClass(source);
                    Object singleton = clazz.newInstance();
                    element = (Element) singleton;                    
                }
            } else if (type != null && type.startsWith("application/x-")) {
                String name = type.substring("application/x-".length());
                String script = (String) struct.get("script");
                String source = (String) struct.get("source");
                element = null;
                if (script != null) {
                    InputStream input = ElementMapFactory.class.getResourceAsStream(path + script);
                    if (input == null) {
                        throw new IOException(path + script + " not found");
                    }
                    InputStreamReader reader = new InputStreamReader(input, "UTF-8");
                    element = new ScriptElement(name, InputStreamService.getString(reader));
                } else if (source != null) {
                    element = new ScriptElement(name, source);                    
                }
            }            
            else {
                // Java                
                Class clazz = Class.forName(clazzName);
                Method factoryMethod = clazz.getDeclaredMethod("getInstance");
                Object singleton = factoryMethod.invoke(null);
                element = (Element) singleton;
            }
            // set settings
            element.setSettings(struct);
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
