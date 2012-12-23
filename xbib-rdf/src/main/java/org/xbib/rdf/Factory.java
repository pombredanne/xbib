package org.xbib.rdf;

import org.xbib.iri.IRI;
import org.xbib.rdf.simple.SimpleBlankNode;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleProperty;
import org.xbib.rdf.simple.SimpleResource;

public final class Factory<S,P,O> {

    private final static transient Factory instance = new Factory();
    
    private Factory() {
    }
    
    public static Factory getInstance() {
        return instance;
    }
    
    public S asSubject(Object subject) {
        Resource<S,P,O> resource = new SimpleResource();
        return subject instanceof Resource ?
                (S)resource :
                resource.toSubject(subject);
    }

    public P asPredicate(Object predicate) {
        return predicate instanceof IRI ? 
               (P)SimpleProperty.create((IRI)predicate) :
               (P)SimpleProperty.create(predicate.toString());
    }

    public static Property create(IRI uri) {
        return SimpleProperty.create(uri);
    }

    public static Property create(String uri) {
        return SimpleProperty.create(IRI.create(uri));
    }

    public Property newProperty(IRI uri) {
        return SimpleProperty.create(uri);
    }

    public Property newProperty(String uri) {
        return SimpleProperty.create(IRI.create(uri));
    }
    
    public BlankNode<S, P, O> newBlankNode() {
        return (BlankNode<S, P, O>) new SimpleBlankNode();
    }

    public BlankNode<S, P, O> newBlankNode(String nodeID) {
        return (BlankNode<S, P, O>) new SimpleBlankNode(nodeID);
    }    
    
    public Literal newLiteral(String value) {
        return new SimpleLiteral().object(value);
    }
    
    public O asObject(Object object) {
        return object == null ? null
                : object instanceof Literal ? (O) object
                : object instanceof IRI
                ? BlankNode.PREFIX.equals(((IRI) object).getScheme())
                ? (O) new SimpleBlankNode((IRI) object)
                : (O) new SimpleResource((IRI) object)
                : (O) new SimpleLiteral(object.toString());
    }    
   
}
