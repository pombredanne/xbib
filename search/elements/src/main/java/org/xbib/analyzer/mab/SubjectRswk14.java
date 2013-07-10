package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk14 extends MABElement {
    
    private final static MABElement element = new SubjectRswk14();
    
    private SubjectRswk14() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
