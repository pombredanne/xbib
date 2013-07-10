package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectDdcDnb extends MABElement {
    
    private final static MABElement element = new SubjectDdcDnb();
    
    private SubjectDdcDnb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
