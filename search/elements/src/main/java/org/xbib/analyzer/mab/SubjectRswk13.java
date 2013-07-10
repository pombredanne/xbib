package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk13 extends MABElement {
    
    private final static MABElement element = new SubjectRswk13();
    
    private SubjectRswk13() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
