package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk12 extends MABElement {
    
    private final static MABElement element = new SubjectRswk12();
    
    private SubjectRswk12() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
