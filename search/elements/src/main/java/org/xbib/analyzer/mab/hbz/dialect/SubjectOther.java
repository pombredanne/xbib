package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectOther extends MABElement {
    
    private final static MABElement element = new SubjectOther();
    
    private SubjectOther() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
