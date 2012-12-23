package org.xbib.rdf;


public interface Node {

    void accept(Visitor visitor);
}
