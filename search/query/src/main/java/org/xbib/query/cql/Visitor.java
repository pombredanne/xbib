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
 *  CQL abstract syntax tree visitor
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public interface Visitor {

    void visit(SortedQuery node);

    void visit(Query node);

    void visit(PrefixAssignment node);

    void visit(ScopedClause node);

    void visit(BooleanGroup node);

    void visit(SearchClause node);

    void visit(Relation node);

    void visit(Modifier node);

    void visit(ModifierList node);

    void visit(Term node);

    void visit(Identifier node);

    void visit(Index node);

    void visit(SimpleName node);

    void visit(SortSpec node);

    void visit(SingleSpec node);

}
