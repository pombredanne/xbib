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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.control.CompilationFailedException;
import org.xbib.elements.output.ElementOutput;
import org.xbib.rdf.ResourceContext;

public abstract class AbstractElementBuilder<K, V, E extends Element, C extends ResourceContext>
        implements ElementBuilder<K, V, E, C> {

    protected final ThreadLocal<C> context = new ThreadLocal();
    private final List<ElementOutput> outputs = new ArrayList();
    private final static String ROOT_PATH = "/org/xbib/elements/output/";
    private final GroovyClassLoader gcl = new GroovyClassLoader();

    protected abstract ElementContextFactory<C> getContextFactory();

    public AbstractElementBuilder(String... paths) {
        for (String path : paths) {
            InputStream groovy = ElementMapFactory.class.getResourceAsStream(ROOT_PATH + path + ".groovy");
            if (groovy != null) {
                try {
                    InputStreamReader reader = new InputStreamReader(groovy, "UTF-8");
                    GroovyCodeSource gcs = new GroovyCodeSource(reader, "class", ROOT_PATH);
                    Class clazz = gcl.parseClass(gcs);
                    outputs.add((ElementOutput) clazz.newInstance());
                } catch (UnsupportedEncodingException | CompilationFailedException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public AbstractElementBuilder<K, V, E, C> addOutput(ElementOutput output) {
        outputs.add(output);
        return this;
    }

    @Override
    public void begin() {
        C c = getContextFactory().newContext();
        c.setResource(c.newResource());
        context.set(c);
    }

    @Override
    public void build(E element, K key, V value) {
        // dummy action
        context.get().resource().addProperty("class:" + getClass().getSimpleName(), value);
    }

    @Override
    public void end() {
        end(null);
    }

    @Override
    public void end(Object info) {
        for (ElementOutput output : outputs) {
            if (output.enabled()) {
                output.output(context.get(), info);
            }
        }
    }

    @Override
    public C context() {
        return context.get();
    }
    
    public List<ElementOutput> getOutputs() {
        return outputs;
    }
}
