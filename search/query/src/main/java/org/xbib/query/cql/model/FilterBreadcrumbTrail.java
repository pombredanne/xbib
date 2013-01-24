/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.query.cql.model;

import java.util.Iterator;
import java.util.TreeSet;
import org.xbib.query.BreadcrumbTrail;
import org.xbib.query.cql.BooleanOperator;

/**
 * Filter breadcrumbs
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class FilterBreadcrumbTrail extends TreeSet<Filter>
        implements BreadcrumbTrail<Filter> {

    private BooleanOperator op;
    /**
     * Constructor
     */
    public FilterBreadcrumbTrail(BooleanOperator op) {
        super();
        this.op = op;
    }

    @Override
    public String toString() {
        return toCQL();
    }

    public String toCQL() {
        StringBuilder sb = new StringBuilder();
        if (isEmpty()) {
            return sb.toString();
        }
        if (op == BooleanOperator.OR && size() > 1) {
            sb.append('(');
        }
        Iterator<Filter> it = this.iterator();
        sb.append(it.next().toCQL());
        while (it.hasNext()) {
            sb.append(' ').append(op).append(' ').append(it.next().toCQL());
        }
        if (op == BooleanOperator.OR && size() > 1) {
            sb.append(')');
        }
        return sb.toString();
    }
}
