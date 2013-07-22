package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectBay extends MABElement {
    
    private final static MABElement element = new SubjectBay();
    
    private SubjectBay() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
