package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain02 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain02();
    
    private SubjectRswkChain02() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
