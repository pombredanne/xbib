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

import java.net.URI;
import org.xbib.rdf.AbstractSequence;
import org.xbib.rdf.BlankNode;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;

/**
 * A simple resource is a sequence of properties and of associated resources.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SimpleResource<S extends Resource<?, ?, ?>, P extends Property, O extends Literal<?>>
        extends AbstractSequence<S, P, O> {

    public SimpleResource() {
        super();
    }

    public SimpleResource(URI identifier) {
        super(identifier);
    }

    @SuppressWarnings("unchecked")
    protected SimpleResource(String value) {
        super((O) new SimpleLiteral<String>(value));
    }

    @Override
    public Literal<?> createLiteral(String value) {
        return new SimpleLiteral<>(value);
    }

    @Override
    public Literal<?> createLiteral(String value, String language) {
        return new SimpleLiteral<>(value, language);
    }

    @Override
    public Literal<?> createLiteral(String value, URI encodingScheme) {
        return new SimpleLiteral<>(value, encodingScheme);
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlankNode<S, P, O> createBlankNode() {
        return (BlankNode<S, P, O>) new SimpleBlankNode<S, P, S>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public BlankNode<S, P, O> createBlankNode(String nodeID) {
        return (BlankNode<S, P, O>) new SimpleBlankNode<S, P, S>(nodeID);
    }

    @Override
    public S createSubject(Object subject) {
        if (subject instanceof URI) {
            URI uri = (URI) subject;
            if (BlankNode.PREFIX.equals(uri.getScheme())) {
                return (S) new SimpleBlankNode<S, P, S>(uri);
            }
        }
        return subject != null
                ? (S) new SimpleResource<S, P, O>(subject.toString())
                : (S) new SimpleResource<S, P, O>();
    }

    @Override
    public O createObject(Object object) {
        return object == null ? null
                : object instanceof Literal ? (O) object
                : object instanceof URI
                ? BlankNode.PREFIX.equals(((URI) object).getScheme())
                ? (O) new SimpleBlankNode((URI) object)
                : (O) new SimpleResource((URI) object)
                : (O) new SimpleLiteral<String>(object.toString());
    }

    @Override
    public Statement<S, P, O> createStatement(S subject, P predicate, O object) {
        if (subject == null) {
            throw new IllegalArgumentException("subject is null");
        }
        if (predicate == null) {
            throw new IllegalArgumentException("predicate is null");
        }
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }
        return new SimpleStatement<S, P, O>(subject, predicate, object);
    }

    @Override
    public int compareTo(Resource<S, P, O> o) {
        return getIdentifier() == null ? -1 : getIdentifier().toString().compareTo(o.getIdentifier().toString());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (getIdentifier() != null ? getIdentifier().hashCode() : 0);
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
        // ????
        return true;
    }
}
