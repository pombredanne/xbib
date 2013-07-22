package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectRvk extends MABElement {
    
    private final static MABElement element = new SubjectRvk();
    
    private SubjectRvk() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
