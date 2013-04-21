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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.xbib.iri.IRI;
import org.xbib.rdf.AbstractResource;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.Visitor;

/**
 * A simple resource is a sequence of properties and of associated resources.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SimpleResource<S extends Identifier, P extends Property, O extends Node>
        extends AbstractResource<S, P, O> {

    private transient final Factory<S,P,O> factory = Factory.getInstance();
    
    @Override
    public Resource<S,P,O> newResource(P predicate) {
        Resource<S,P,O> r = new SimpleResource().id(new IdentifiableNode().blank().id());
        attributes.put(predicate, r);
        return r;
    }

    @Override
    public Resource<S, P, O> add(String predicate, String value) {
        return add(factory.asPredicate(predicate), value);
    }

    @Override
    public Resource<S, P, O> add(String predicate, Integer value) {
        return add(factory.asPredicate(predicate), value);
    }

    @Override
    public Resource<S, P, O> add(String predicate, Literal value) {
        return add(factory.asPredicate(predicate), value);
    }
    
    @Override
    public Resource<S, P, O> add(String predicate, IRI externalResource) {
        return add(factory.asPredicate(predicate), externalResource);
    }

    @Override
    public Resource<S, P, O> add(String predicate, Collection literals) {
        return add(factory.asPredicate(predicate), literals);
    }

    @Override
    public Resource<S, P, O> add(P predicate, String value) {
        return add(predicate, factory.asLiteral(value));
    }

    @Override
    public Resource<S, P, O> add(P predicate, Integer value) {
        return add(predicate, factory.asLiteral(value));
    }

    @Override
    public Resource<S, P, O> add(P predicate, Collection literals) {
        for (Object object : literals) {
            add(predicate, factory.asLiteral(object));
        }
        return this;
    }

    @Override
    public Resource<S, P, O> newResource(IRI predicate) {
        return newResource(factory.asPredicate(predicate));
    }

    @Override
    public Resource<S, P, O> newResource(String predicate) {
        return newResource(factory.asPredicate(predicate));
    }
    
    
    @Override
    public Resource<S, P, O> add(String predicate, Resource<S, P, O> resource) {
        return add(factory.asPredicate(predicate), resource);
    }
    
    @Override
    public Set<P> predicateSet(String subject) {
        return predicateSet(factory.asSubject(subject));
    }

    @Override
    public Collection<O> objects(String predicate) {
        return objects(factory.asPredicate(predicate));
    }

    @Override
    public O literal(String predicate) {
        return literal(factory.asPredicate(predicate));
    }

    @Override
    public Iterator<Triple<S, P, O>> iterator() {
        return new TripleIterator(this, true);
    }

    @Override
    public Iterator<Triple<S,P,O>> propertyIterator() {
        return new TripleIterator(this, false);
    }

    @Override
    public int compareTo(Resource<S, P, O> o) {
        return id() == null ? -1 : id().toString().compareTo(o.id().toString());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (id() != null ? id().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public Resource<S, P, O> type(IRI type) {
        add(factory.rdfType(), type);
        return this;
    }

    public IRI type() {
        Collection<Node> c = attributes.get(factory.rdfType());
        Node node = c != null ? c.iterator().hasNext() ?
                c.iterator().next()
                : null : null;
        return ((Resource) node).id();
    }

    public Resource<S, P, O> language(String lang) {
        add(factory.rdfLang(), lang);
        return this;
    }

    public String language() {
        Collection<Node> c = attributes.get(factory.rdfLang());
        return c != null ? c.iterator().hasNext() ?
                c.iterator().next().toString()
                : null : null;
    }    

}
