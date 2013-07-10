package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswkChain08 extends MABElement {
    
    private final static MABElement element = new SubjectRswkChain08();
    
    private SubjectRswkChain08() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
