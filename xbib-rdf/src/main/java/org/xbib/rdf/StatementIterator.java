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
package org.xbib.rdf;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Iterate over a resource and generate statements 
 * 
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 *
 * @param <S>
 * @param <P>
 * @param <O>
 */
public class StatementIterator<S, P, O> implements Iterator<Statement<S, P, O>> {

    private final LinkedList<Statement<S, P, O>> statements;
    private final boolean recursion;

    public StatementIterator(Resource<S, P, O> resource, boolean recursion) {
        this.recursion = recursion;
        this.statements = resource != null ? unfold(resource) : new LinkedList();
    }

    @Override
    public boolean hasNext() {
        return !statements.isEmpty();
    }

    @Override
    public Statement<S, P, O> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Statement<S, P, O> statement = statements.poll();
        if (recursion && statement.getObject() instanceof BlankNode) {
            statements.addAll(0, unfold((Resource<S, P, O>) statement.getObject()));
        }
        return statement;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private LinkedList<Statement<S, P, O>> unfold(Resource<S, P, O> resource) {
        LinkedList<Statement<S, P, O>> list = new LinkedList<Statement<S, P, O>>();
        S subj = resource.subject();
        for (P pred : resource.predicateSet(subj)) {
            for (O obj : resource.objectSet(pred)) {
                list.offer(resource.newStatement(subj, pred, obj));
            }
        }
        return list;
    }
}
