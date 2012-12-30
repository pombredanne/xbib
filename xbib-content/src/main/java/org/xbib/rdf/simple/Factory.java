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
                new SimpleLiteral(literal.toString());
    }
    
    public O asObject(Object object) {
        return object == null ? null :
                object instanceof Literal ? (O) object :
                object instanceof IRI ? (O) new SimpleResource().id((IRI) object) :
                (O) new SimpleLiteral(object.toString());
    }

    public Identifier newBlankNode(String nodeID) {
        return new IdentifiableNode().id(nodeID);
    }    
    
    public Literal newLiteral(String value) {
        return new SimpleLiteral().object(value);
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
