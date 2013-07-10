package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectKab extends MABElement {
    
    private final static MABElement element = new SubjectKab();
    
    private SubjectKab() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
