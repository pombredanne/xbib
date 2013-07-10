package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain01 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain01();
    
    private SubjectRswkChain01() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
