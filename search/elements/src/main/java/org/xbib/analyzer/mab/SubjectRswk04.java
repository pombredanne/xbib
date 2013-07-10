package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk04 extends MABElement {
    
    private final static MABElement element = new SubjectRswk04();
    
    private SubjectRswk04() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
