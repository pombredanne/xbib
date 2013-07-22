package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectTum extends MABElement {
    
    private final static MABElement element = new SubjectTum();
    
    private SubjectTum() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
