package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectZdb extends MABElement {
    
    private final static MABElement element = new SubjectZdb();
    
    private SubjectZdb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
