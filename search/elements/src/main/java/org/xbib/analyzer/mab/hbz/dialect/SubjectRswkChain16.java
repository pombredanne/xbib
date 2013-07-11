package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain16 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain16();
    
    private SubjectRswkChain16() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
