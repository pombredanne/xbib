package org.xbib.elements.mab;

import java.net.URI;
import org.xbib.marc.FieldCollection;

public class IdentifierRecord extends MABElement {
    
    private final static MABElement element = new IdentifierRecord();
    
    private IdentifierRecord() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldCollection key, String value) {
        value = value.trim();
        b.context().resource().setIdentifier(URI.create("http://xbib.org#" + value)); // temporal ID
        b.context().getResource(b.context().resource(), IDENTIFIER).addProperty(XBIB_IDENTIFIER_AUTHORITY_MAB, value);
    }

}
