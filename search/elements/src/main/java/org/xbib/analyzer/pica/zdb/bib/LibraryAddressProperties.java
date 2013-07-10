package org.xbib.analyzer.pica.zdb.bib;

import org.xbib.rdf.Node;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Property;
import org.xbib.rdf.simple.SimpleFactory;

public interface LibraryAddressProperties {
    
    final SimpleFactory<Identifier,Property,Node> SIMPLE_FACTORY = SimpleFactory.getInstance();
    
    String LA_NS_URI = "http://xbib.org/libraryaddress/v1/";

}
