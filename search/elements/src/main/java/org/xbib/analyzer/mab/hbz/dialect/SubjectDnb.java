package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectDnb extends MABElement {
    
    private final static MABElement element = new SubjectDnb();
    
    private SubjectDnb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
