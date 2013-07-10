package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain14 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain14();
    
    private SubjectRswkChain14() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
