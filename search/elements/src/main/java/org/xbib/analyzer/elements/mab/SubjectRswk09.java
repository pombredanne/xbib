package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class SubjectRswk09 extends MABElement {
    
    private final static MABElement element = new SubjectRswk09();
    
    private SubjectRswk09() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public SubjectRswk09 build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
