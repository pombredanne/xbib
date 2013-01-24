package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class SubjectRswkChain09 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain09();
    
    private SubjectRswkChain09() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public SubjectRswkChain09 build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
