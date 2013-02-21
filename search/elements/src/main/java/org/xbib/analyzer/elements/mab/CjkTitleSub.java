package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class CjkTitleSub extends MABElement {
    
    private final static MABElement element = new CjkTitleSub();
    
    private CjkTitleSub() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public CjkTitleSub build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
