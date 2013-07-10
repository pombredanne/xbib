package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain15 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain15();
    
    private SubjectRswkChain15() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
