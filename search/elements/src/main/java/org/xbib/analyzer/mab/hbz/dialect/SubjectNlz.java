package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectNlz extends MABElement {
    
    private final static MABElement element = new SubjectNlz();
    
    private SubjectNlz() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
