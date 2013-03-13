package org.xbib.analyzer.elements.pica.zdb.bib;

import org.xbib.rdf.Node;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Property;
import org.xbib.rdf.simple.Factory;

public interface LibraryAddressProperties {
    
    final Factory<Identifier,Property,Node> factory = Factory.getInstance();
    
    String LA_NS_URI = "http://xbib.org/libraryaddress/v1/";

}
