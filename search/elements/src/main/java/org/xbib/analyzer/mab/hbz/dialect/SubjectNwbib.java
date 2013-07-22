package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectNwbib extends MABElement {
    
    private final static MABElement element = new SubjectNwbib();
    
    private SubjectNwbib() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
