package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectDdcDnb extends MABElement {
    
    private final static MABElement element = new SubjectDdcDnb();
    
    private SubjectDdcDnb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
