package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain04 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain04();
    
    private SubjectRswkChain04() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
