package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.iri.IRI;
import org.xbib.marc.FieldCollection;

public class IdentifierRecord extends MABElement {
    
    private final static MABElement element = new IdentifierRecord();
    
    private IdentifierRecord() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public IdentifierRecord build(MABBuilder b, FieldCollection key, String value) {
        value = value.trim();
        b.context().resource().id(IRI.create("http://xbib.org#" + value)); // temporal ID
        b.context().getResource(b.context().resource(), IDENTIFIER).add(XBIB_IDENTIFIER_AUTHORITY_MAB, value);
        return this;
    }

}
