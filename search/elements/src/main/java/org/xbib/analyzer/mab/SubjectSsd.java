package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectSsd extends MABElement {
    
    private final static MABElement element = new SubjectSsd();
    
    private SubjectSsd() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
