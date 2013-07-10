package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain12 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain12();
    
    private SubjectRswkChain12() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
