package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain10 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain10();
    
    private SubjectRswkChain10() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
