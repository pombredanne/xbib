package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectDdc extends MABElement {
    
    private final static MABElement element = new SubjectDdc();
    
    private SubjectDdc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
