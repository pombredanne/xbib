package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain06 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain06();
    
    private SubjectRswkChain06() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
