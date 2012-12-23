package org.xbib.analyzer.elements.pica.zdb.bib;

import org.xbib.rdf.Factory;
import org.xbib.rdf.Property;

public interface LibraryAddressProperties {
    
    final Factory factory = Factory.getInstance();
    
    String LA_NS_URI = "http://xbib.org/libraryaddress/v1/";

    Property LA_IDENTIIFER = Factory.create(LA_NS_URI + "identifierAuthorityLA");
    Property LA_IDENTIIFER_DBS = Factory.create(LA_NS_URI + "identifierAuthorityDBS");
    Property LA_IDENTIIFER_SIGEL = Factory.create(LA_NS_URI + "identifierAuthoritySigel");
    Property LA_IDENTIIFER_ISIL = Factory.create(LA_NS_URI + "identifierAuthorityISIL");
    Property LA_IDENTIIFER_OCLC = Factory.create(LA_NS_URI + "identifierAuthorityOCLC");
    Property LA_NAME = Factory.create(LA_NS_URI + "name");
    Property LA_SHORTNAME = Factory.create(LA_NS_URI + "shortName");
}
