package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectTum extends MABElement {
    
    private final static MABElement element = new SubjectTum();
    
    private SubjectTum() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
