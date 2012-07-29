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
package org.xbib.io.iso23950.pqf;

import java.util.LinkedList;

/**
 * PQF abstract syntax tree
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class Query extends Node {

    private String schema;
    private LinkedList<AttrSpec> attrspec = new LinkedList();
    private Query value;
    private String set;
    private String name;
    private Expression expr;
    private PQF pqf;

    public Query(String schema, AttrSpec attrspec, Query value) {
        this.schema = schema;
        this.attrspec.add(attrspec);
        this.value = value;
        this.name = value.getName();
        this.attrspec.addAll(value.getAttrSpec());
    }

    // ATTR attrspec querystruct
    public Query(AttrSpec attrspec, Query value) {
        this.attrspec.add(attrspec);
        this.value = value;
        this.name = value.getName();
        this.attrspec.addAll(value.getAttrSpec());
    }

    public Query(String set, String name) {
        this.set = set;
        this.name = name;
    }

    public Query(String name) {
        this.name = name;
    }

    public Query(Expression expr) {
        this.expr = expr;
    }

    public Query(PQF pqf) {
        this.pqf = pqf;
    }

    public void accept(Visitor visitor) {
        if (expr != null) {
            expr.accept(visitor);
        }
        if (value != null) {
            value.accept(visitor);
        }
        for (AttrSpec attr : attrspec) {
            attr.accept(visitor);
        }
        if (pqf != null) {
            pqf.accept(visitor);
        }
        visitor.visit(this);
    }

    public String getSchema() {
        return schema;
    }

    public String getSet() {
        return set;
    }

    public String getName() {
        return name;
    }

    public LinkedList<AttrSpec> getAttrSpec() {
        return attrspec;
    }

    @Override
    public String toString() {
        return "[Query: name=" + name + " schema=" + schema + " set=" + set + " value=" + value + "]";
    }
}
