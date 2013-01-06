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
                (S)new SimpleResource().id(IRI.create(subject.toString()));
    }

    public P asPredicate(Object predicate) {
        return predicate instanceof Property ?
                (P)predicate :
                (P)new IdentifiableProperty(IRI.create(predicate.toString()));
    }

    public Literal asLiteral(Object literal) {
        return literal == null ? null :
                literal instanceof Literal ? (Literal)literal :
                newLiteral(literal);
    }
    
    public O asObject(Object object) {
        return object == null ? null :
                object instanceof Literal ? (O) object :
                object instanceof IRI ? (O) new SimpleResource().id((IRI) object) :
                (O) new SimpleLiteral(object);
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
