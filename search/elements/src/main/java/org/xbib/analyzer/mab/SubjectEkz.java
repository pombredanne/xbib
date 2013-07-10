package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectEkz extends MABElement {
    
    private final static MABElement element = new SubjectEkz();
    
    private SubjectEkz() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
