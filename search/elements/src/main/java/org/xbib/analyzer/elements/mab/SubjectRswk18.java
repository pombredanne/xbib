package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class SubjectRswk18 extends MABElement {
    
    private final static MABElement element = new SubjectRswk18();
    
    private SubjectRswk18() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public SubjectRswk18 build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
