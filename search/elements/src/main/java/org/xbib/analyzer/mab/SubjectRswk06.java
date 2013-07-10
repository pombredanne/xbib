package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk06 extends MABElement {
    
    private final static MABElement element = new SubjectRswk06();
    
    private SubjectRswk06() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
