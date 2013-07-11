package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectKab extends MABElement {
    
    private final static MABElement element = new SubjectKab();
    
    private SubjectKab() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
