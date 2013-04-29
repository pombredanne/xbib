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

import org.xbib.iri.IRI;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.IdentifiableProperty;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.RDF;

public final class Factory<S,P,O> {

    private final static transient Factory instance = new Factory();

    private Factory() {
    }
    
    public static <S,P,O> Factory<S,P,O> getInstance() {
        return instance;
    }
    
    public S asSubject(Object subject) {
        return subject instanceof Identifier ? (S)subject :
                subject instanceof IRI ? (S)new IdentifiableNode().id((IRI)subject) :
                (S)new SimpleResource().id(IRI.builder().curi(subject.toString()).build());
    }

    public P asPredicate(Object predicate) {
        return predicate == null ? null :
                predicate instanceof Property ? (P)predicate :
                predicate instanceof IRI ?  (P)new IdentifiableProperty((IRI)predicate) :
                (P)new IdentifiableProperty(IRI.builder().curi(predicate.toString()).build());
    }

    public O asObject(Object object) {
        return object == null ? null :
                object instanceof Literal ? (O) object :
                object instanceof IRI ? (O) new SimpleResource().id((IRI)object) :
                (O) new SimpleLiteral(object);
    }

    public Literal asLiteral(Object literal) {
        return literal == null ? null :
                literal instanceof Literal ? (Literal)literal :
                        newLiteral(literal);
    }

    public Identifier newBlankNode(String nodeID) {
        return new IdentifiableNode().id(nodeID);
    }    
    
    public Literal newLiteral(Object value) {
        Literal l = new SimpleLiteral();
        if (value instanceof Double) {
            return l.type(Literal.XSD_DOUBLE).object(value);
        }
        if (value instanceof Float) {
            return l.type(Literal.XSD_FLOAT).object(value);
        }
        if (value instanceof Long) {
            return l.type(Literal.XSD_LONG).object(value);
        }
        if (value instanceof Integer) {
            return l.type(Literal.XSD_INT).object(value);
        }
        if (value instanceof Boolean) {
            return l.type(Literal.XSD_BOOLEAN).object(value);
        }
        // auto derive
        return l.object(value);        
    }

    private final static Property TYPE = new IdentifiableProperty(RDF.RDF_TYPE);

    public P rdfType() {
        return (P)TYPE;
    }

    private final static Property LANG = new IdentifiableProperty(RDF.RDF_LANGUAGE);

    public P rdfLang() {
        return (P)LANG;
    }

}
