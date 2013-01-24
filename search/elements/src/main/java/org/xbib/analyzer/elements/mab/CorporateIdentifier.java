package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class CorporateIdentifier extends MABElement {
    
    private final static MABElement element = new CorporateIdentifier();
    
    private CorporateIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public CorporateIdentifier build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
