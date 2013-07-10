package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;


public class SubjectRswkChain20 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain20();
    
    private SubjectRswkChain20() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
