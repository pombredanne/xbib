package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain19 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain19();
    
    private SubjectRswkChain19() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
