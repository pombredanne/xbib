package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectDdc extends MABElement {
    
    private final static MABElement element = new SubjectDdc();
    
    private SubjectDdc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
