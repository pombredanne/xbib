package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectBnb extends MABElement {
    
    private final static MABElement element = new SubjectBnb();
    
    private SubjectBnb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
