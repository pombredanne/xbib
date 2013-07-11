package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain03 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain03();
    
    private SubjectRswkChain03() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
