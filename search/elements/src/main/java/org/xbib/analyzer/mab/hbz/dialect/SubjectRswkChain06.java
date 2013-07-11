package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain06 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain06();
    
    private SubjectRswkChain06() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
