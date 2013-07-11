package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain14 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain14();
    
    private SubjectRswkChain14() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
