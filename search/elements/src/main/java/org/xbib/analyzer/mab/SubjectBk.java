package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectBk extends MABElement {
    
    private final static MABElement element = new SubjectBk();
    
    private SubjectBk() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
