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
package org.xbib.federator.action;

import java.util.Map;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public abstract class AbstractAction implements Action {

    private String group;

    protected Map<String, Object> params;

    protected SearchRetrieveResponse response;

    protected StylesheetTransformer transformer;

    protected long count = 0L;

    protected String base;

    @Override
    public Action setBase(String base) {
        this.base = base;
        return this;
    }
    
    public String getBase() {
        return base;
    }
    
    @Override
    public Action setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public Action setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Action setResponse(SearchRetrieveResponse response) {
        this.response = response;
        return this;
    }
    
    @Override
    public SearchRetrieveResponse getResponse() {
        return response;
    }

    public Action setTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public long getCount() {
        return count;
    }

    protected String get(Map<String, Object> map, String key, String defaultValue) {
        return map.containsKey(key) ? map.get(key).toString() : defaultValue;
    }

    protected int get(Map<String, Object> map, String key, int defaultValue) {
        return map.containsKey(key) ? (Integer) map.get(key) : defaultValue;
    }

    protected boolean get(Map<String, Object> map, String key, boolean defaultValue) {
        return map.containsKey(key) ? (Boolean) map.get(key) : defaultValue;
    }
}
