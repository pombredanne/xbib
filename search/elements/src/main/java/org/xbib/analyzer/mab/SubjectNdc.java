package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectNdc extends MABElement {
    
    private final static MABElement element = new SubjectNdc();
    
    private SubjectNdc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
