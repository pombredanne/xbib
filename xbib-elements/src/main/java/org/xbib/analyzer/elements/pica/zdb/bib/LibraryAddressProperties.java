package org.xbib.analyzer.elements.pica.zdb.bib;

import org.xbib.rdf.Node;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Property;
import org.xbib.rdf.simple.Factory;

public interface LibraryAddressProperties {
    
    final Factory<Identifier,Property,Node> factory = Factory.getInstance();
    
    String LA_NS_URI = "http://xbib.org/libraryaddress/v1/";

    Property LA_IDENTIIFER = factory.asPredicate(LA_NS_URI + "identifierAuthorityLA");
    Property LA_IDENTIIFER_DBS = factory.asPredicate(LA_NS_URI + "identifierAuthorityDBS");
    Property LA_IDENTIIFER_SIGEL = factory.asPredicate(LA_NS_URI + "identifierAuthoritySigel");
    Property LA_IDENTIIFER_ISIL = factory.asPredicate(LA_NS_URI + "identifierAuthorityISIL");
    Property LA_IDENTIIFER_OCLC = factory.asPredicate(LA_NS_URI + "identifierAuthorityOCLC");
    Property LA_NAME = factory.asPredicate(LA_NS_URI + "name");
    Property LA_SHORTNAME = factory.asPredicate(LA_NS_URI + "shortName");
}
