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

import java.util.ArrayList;
import java.util.List;

/**
 *  CQL query
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class Query extends AbstractNode {

    private List<PrefixAssignment> prefixes = new ArrayList();
    private Query query;
    private ScopedClause clause;

    Query(Query query) {
        this.query = query;
    }

    Query(PrefixAssignment assignment, Query query) {
        prefixes.add(assignment);
        this.query = query;
    }

    Query(ScopedClause clause) {
        this.clause = clause;
    }

    public List<PrefixAssignment> getPrefixAssignments() {
        return prefixes;
    }

    public Query getQuery() {
        return query;
    }

    public ScopedClause getScopedClause() {
        return clause;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (PrefixAssignment  assignment : prefixes) {
            sb.append(assignment.toString()).append(' ');
        }
        if (query != null) sb.append(query);
        if (clause != null) sb.append(clause);
        return sb.toString();
    }

}
