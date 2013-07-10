package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk18 extends MABElement {
    
    private final static MABElement element = new SubjectRswk18();
    
    private SubjectRswk18() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
