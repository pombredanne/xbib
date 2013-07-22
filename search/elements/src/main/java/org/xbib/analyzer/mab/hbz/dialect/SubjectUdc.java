package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectUdc extends MABElement {
    
    private final static MABElement element = new SubjectUdc();
    
    private SubjectUdc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
