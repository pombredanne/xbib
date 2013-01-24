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
package org.xbib.query.cql.elasticsearch.model;

import org.xbib.query.Breadcrumb;
import org.xbib.query.QueryFacet;

public final class Facet<V> implements QueryFacet<V> {

    public enum Type {
        TERMS, RANGE, HISTOGRAM, DATEHISTOGRAM, FILTER, QUERY, 
        STATISTICAL, TERMS_STATS, GEO_DISTANCE
    }
    
    public static int DEFAULT_FACET_SIZE = 10;
    
    private String name;
    private Type type;
    private V value;
    private int size;

    public Facet(String name, Type type, V value) {
        this(name, type, value, DEFAULT_FACET_SIZE);
    }

    public Facet(String name, Type type, V value, int size) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.size = size;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
    
    @Override
    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getFilterName() {        
        return name;
    }

    @Override
    public int compareTo(Breadcrumb o) {
        return name.compareTo(((Facet)o).getName());
    }

    @Override
    public String toString() {
        return "facet [name=" + name +",value=" + value + ",size=" + size  + "]";
    }

}
