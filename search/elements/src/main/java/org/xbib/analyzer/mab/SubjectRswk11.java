package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk11 extends MABElement {
    
    private final static MABElement element = new SubjectRswk11();
    
    private SubjectRswk11() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
