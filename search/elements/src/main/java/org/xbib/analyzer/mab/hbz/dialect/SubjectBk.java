package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectBk extends MABElement {
    
    private final static MABElement element = new SubjectBk();
    
    private SubjectBk() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
