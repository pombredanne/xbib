package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain18 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain18();
    
    private SubjectRswkChain18() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
