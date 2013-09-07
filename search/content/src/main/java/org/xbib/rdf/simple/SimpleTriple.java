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
package org.xbib.rdf.simple;

import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Triple;

/**
 * A simple triple
 *
 */
public class SimpleTriple<S extends Identifier, P extends Property, O extends Node>
        implements Triple, Comparable<Triple> {
    
    private final SimpleFactory<S,P,O> simpleFactory = SimpleFactory.getInstance();

    private S subject;
    private P predicate;
    private O object;

    /**
     * Create a new triple
     *
     * @param subject
     * @param predicate
     * @param object
     */
    public SimpleTriple(S subject, P predicate, O object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }
    
    public SimpleTriple(Object subject, Object predicate, Object object) {
        this.subject = simpleFactory.asSubject(subject);
        this.predicate = simpleFactory.asPredicate(predicate);
        this.object = simpleFactory.asObject(object);
    }

    public void subject(S subject) {
        this.subject = subject;
    }

    @Override
    public S subject() {
        return subject;
    }

    public void predicate(P predicate) {
        this.predicate = predicate;
    }

    @Override
    public P predicate() {
        return predicate;
    }

    public void object(O object) {
        this.object = object;
    }

    @Override
    public O object() {
        return object;
    }

    @Override
    public String toString() {
        return (subject != null ? subject : " <null>")
                + (predicate != null ? " " + predicate : " <null>")
                + (object != null ? " " + object : " <null>");
    }

    @Override
    public int hashCode() {
        return (subject != null ? subject.hashCode() : 0)
                + (predicate != null ? predicate.hashCode() : 0)
                + (object != null ? object.hashCode() : 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((Triple<S, P, O>) obj) == 0;
    }

    @Override
    public int compareTo(Triple triple) {
        return toString().compareTo(triple.toString());
    }
}
