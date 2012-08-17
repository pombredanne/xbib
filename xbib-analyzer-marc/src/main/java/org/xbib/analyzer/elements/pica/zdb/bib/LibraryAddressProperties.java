package org.xbib.analyzer.elements.pica.zdb.bib;

import org.xbib.rdf.Property;

public interface LibraryAddressProperties {
    
    String LA_NS_URI = "http://xbib.org/libraryaddress/v1/";

    Property LA_IDENTIIFER = Property.create(LA_NS_URI + "identifierAuthorityLA");
    Property LA_IDENTIIFER_DBS = Property.create(LA_NS_URI + "identifierAuthorityDBS");
    Property LA_IDENTIIFER_SIGEL = Property.create(LA_NS_URI + "identifierAuthoritySigel");
    Property LA_IDENTIIFER_ISIL = Property.create(LA_NS_URI + "identifierAuthorityISIL");
    Property LA_IDENTIIFER_OCLC = Property.create(LA_NS_URI + "identifierAuthorityOCLC");
    Property LA_NAME = Property.create(LA_NS_URI + "name");
    Property LA_SHORTNAME = Property.create(LA_NS_URI + "shortName");
}
