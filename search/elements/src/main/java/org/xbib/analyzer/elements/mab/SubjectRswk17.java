package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class SubjectRswk17 extends MABElement {
    
    private final static MABElement element = new SubjectRswk17();
    
    private SubjectRswk17() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public SubjectRswk17 build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
