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

import org.xbib.query.cql.model.CQLQueryModel;

/**
 *  Search clause
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SearchClause extends AbstractNode {

    private Query query;
    private Index index;
    private Relation relation;
    private Term term;

    SearchClause(Query query) {
        this.query = query;
    }

    SearchClause(Index index, Relation relation, Term term) {
        this.index = index;
        this.relation = relation;
        this.term = term;
    }

    SearchClause(Term term) {
        this.term = term;
    }

    public Query getQuery() {
        return query;
    }

    public Index getIndex() {
        return index;
    }

    public Term getTerm() {
        return term;
    }

    public Relation getRelation() {
        return relation;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return CQL string
     */
    @Override
    public String toString() {
        return query != null && query.toString().length() > 0 ? "(" + query + ")"
                : query != null ? ""
                : index != null && !CQLQueryModel.isVisible(index.getContext()) ? ""
                : index != null ? index + " " + relation + " " + term
                : term != null ? term.toString()
                : null;
    }

}
