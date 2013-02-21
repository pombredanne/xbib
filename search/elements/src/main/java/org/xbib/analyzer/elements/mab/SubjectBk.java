package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class SubjectBk extends MABElement {
    
    private final static MABElement element = new SubjectBk();
    
    private SubjectBk() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public SubjectBk build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
