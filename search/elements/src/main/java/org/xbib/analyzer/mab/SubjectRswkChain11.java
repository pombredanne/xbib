package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain11 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain11();
    
    private SubjectRswkChain11() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
