package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectMsc extends MABElement {
    
    private final static MABElement element = new SubjectMsc();
    
    private SubjectMsc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
