package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectEkz extends MABElement {
    
    private final static MABElement element = new SubjectEkz();
    
    private SubjectEkz() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
