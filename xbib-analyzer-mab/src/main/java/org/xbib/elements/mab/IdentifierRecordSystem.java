package org.xbib.elements.mab;

import java.net.URI;
import org.xbib.marc.FieldCollection;

public class IdentifierRecordSystem extends MABElement {
    
    private final static MABElement element = new IdentifierRecordSystem();
    
    private IdentifierRecordSystem() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldCollection key, String value) {
        value = value.trim();
        b.context().resource().setIdentifier(URI.create("http://xbib.org#" + value));
        b.context().getResource(b.context().resource(), IDENTIFIER).addProperty(XBIB_IDENTIFIER_AUTHORITY_SYSID, value);
    }

}
