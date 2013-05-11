package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.iri.IRI;
import org.xbib.marc.FieldCollection;

public class IdentifierRecordSystem extends MABElement {
    
    private final static MABElement element = new IdentifierRecordSystem();
    
    private IdentifierRecordSystem() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

   /* @Override
    public IdentifierRecordSystem build(MABBuilder b, FieldCollection key, String value) {
        value = value.trim();
        b.context().resource().id(IRI.create("http://xbib.org#" + value));
        b.context().resource().add(XBIB_IDENTIFIER_AUTHORITY_SYSID, value);
        return this;
    }*/

}
