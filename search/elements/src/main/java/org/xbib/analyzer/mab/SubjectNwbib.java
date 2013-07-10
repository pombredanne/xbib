package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectNwbib extends MABElement {
    
    private final static MABElement element = new SubjectNwbib();
    
    private SubjectNwbib() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
