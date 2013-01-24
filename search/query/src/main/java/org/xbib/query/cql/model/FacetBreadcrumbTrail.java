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

/**
 * Facet breadcrumb trail
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class FacetBreadcrumbTrail extends TreeSet<Facet>
        implements BreadcrumbTrail<Facet> {

    @Override
    public String toString() {
        return toCQL();
    }

    public String toCQL() {
        StringBuilder sb = new StringBuilder();
        if (isEmpty()) {
            return sb.toString();
        }
        Iterator<Facet> it = iterator();
        if (it.hasNext()) {
            sb.append(it.next().toCQL());
        }
        while (it.hasNext()) {
            sb.append(" and ").append(it.next().toCQL());
        }
        return sb.toString();
    }
}
