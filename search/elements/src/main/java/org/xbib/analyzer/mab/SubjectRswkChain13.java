package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain13 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain13();
    
    private SubjectRswkChain13() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
