package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain05 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain05();
    
    private SubjectRswkChain05() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
