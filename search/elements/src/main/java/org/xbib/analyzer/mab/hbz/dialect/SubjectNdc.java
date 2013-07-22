package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectNdc extends MABElement {
    
    private final static MABElement element = new SubjectNdc();
    
    private SubjectNdc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
