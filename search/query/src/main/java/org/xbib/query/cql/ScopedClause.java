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
package org.xbib.query.cql;

/**
 * Scoped clause. This is a recursive data structure with a SearchClause and
 * optionally a ScopedClause.
 * SearchClause and ScopedClause are connected through a BooleanGroup.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ScopedClause extends AbstractNode {

    private ScopedClause clause;
    private BooleanGroup booleangroup;
    private SearchClause search;

    ScopedClause(ScopedClause clause, BooleanGroup bg, SearchClause search) {
        this.clause = clause;
        this.booleangroup = bg;
        this.search = search;
    }

    ScopedClause(SearchClause search) {
        this.search = search;
    }

    public ScopedClause getScopedClause() {
        return clause;
    }

    public BooleanGroup getBooleanGroup() {
        return booleangroup;
    }

    public SearchClause getSearchClause() {
        return search;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        String s = search.toString();
        boolean hasQuery = s.length() > 0;
        return clause != null && hasQuery ? clause + " " + booleangroup + " " + search
            : clause != null ? clause.toString()
            : hasQuery ? search.toString()
            : "";
    }

}
