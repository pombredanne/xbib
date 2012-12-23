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

import java.util.Map;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public abstract class AbstractScriptingElement implements Element {

    private ScriptEngine engine = new ScriptEngineManager().getEngineByName(getScriptEngineName());
    private Map settings;
    private String script;

    public AbstractScriptingElement(String script) throws ScriptException {
        this.script = script;
        engine.eval(script);
    }

    @Override
    public AbstractScriptingElement setSettings(Map settings) {
        try {
            this.settings = settings;
            ScriptContext context = new SimpleScriptContext();
            Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("settings", settings);
            engine.eval(script, bindings);
        } catch (ScriptException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    @Override
    public Map getSettings() {
        return settings;
    }

    @Override
    public AbstractScriptingElement begin() {
        Invocable inv = (Invocable) engine;
        Object obj = engine.get("Element");
        try {
            inv.invokeMethod(obj, "begin");
        } catch (ScriptException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    @Override
    public AbstractScriptingElement build(ElementBuilder builder, Object key, Object value) {
        Invocable inv = (Invocable) engine;
        Object obj = engine.get("Element");
        try {
            inv.invokeMethod(obj, "build", builder, key, value);
        } catch (ScriptException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    @Override
    public AbstractScriptingElement end() {
        Invocable inv = (Invocable) engine;
        Object obj = engine.get("Element");
        try {
            inv.invokeMethod(obj, "end");
        } catch (ScriptException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    abstract protected String getScriptEngineName();
}
