package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk05 extends MABElement {
    
    private final static MABElement element = new SubjectRswk05();
    
    private SubjectRswk05() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
