package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRvk extends MABElement {
    
    private final static MABElement element = new SubjectRvk();
    
    private SubjectRvk() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
