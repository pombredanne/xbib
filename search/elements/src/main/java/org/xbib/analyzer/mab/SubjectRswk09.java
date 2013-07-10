package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk09 extends MABElement {
    
    private final static MABElement element = new SubjectRswk09();
    
    private SubjectRswk09() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
