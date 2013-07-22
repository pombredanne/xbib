package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectSsd extends MABElement {
    
    private final static MABElement element = new SubjectSsd();
    
    private SubjectSsd() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
