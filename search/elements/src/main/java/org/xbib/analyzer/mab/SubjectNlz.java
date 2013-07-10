package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectNlz extends MABElement {
    
    private final static MABElement element = new SubjectNlz();
    
    private SubjectNlz() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
