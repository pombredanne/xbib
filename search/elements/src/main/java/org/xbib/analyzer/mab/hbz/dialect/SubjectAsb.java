package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectAsb extends MABElement {
    
    private final static MABElement element = new SubjectAsb();
    
    private SubjectAsb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
