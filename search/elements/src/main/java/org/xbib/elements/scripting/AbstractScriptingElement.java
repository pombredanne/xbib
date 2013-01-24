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
package org.xbib.elements.scripting;

import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.xbib.elements.Element;
import org.xbib.elements.ElementBuilder;

public abstract class AbstractScriptingElement implements Element {

    private final ScriptEngine engine;
    private final String invocable;
    private final String script;
    private Element element;
    private Map settings;

    public AbstractScriptingElement(String scriptEngineName, String script, String invocable) {
        this.engine = new ScriptEngineManager().getEngineByName(scriptEngineName);
        this.invocable = invocable;
        this.script = script;        
    }

    @Override
    public AbstractScriptingElement setSettings(Map settings) {
        try {
            this.settings = settings;
            engine.eval(script);
            this.element = (Element)engine.get(invocable);
            if (element != null) {
                element.setSettings(settings);
            }
        } catch (ScriptException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    @Override
    public Map getSettings() {
        return settings;
    }

    public Element getElement(){
        return element;
    }    
    
    @Override
    public AbstractScriptingElement begin() {
        Invocable inv = (Invocable) engine;
        try {
            inv.invokeMethod(element, "begin");
        } catch (ScriptException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    @Override
    public AbstractScriptingElement build(ElementBuilder builder, Object key, Object value) {
        Invocable inv = (Invocable) engine;
        try {
            inv.invokeMethod(element, "build", builder, key, value);
        } catch (ScriptException |NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    @Override
    public AbstractScriptingElement end() {
        Invocable inv = (Invocable) engine;
        try {
            inv.invokeMethod(element, "end");
        } catch (ScriptException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }
}
