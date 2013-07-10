package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectOther extends MABElement {
    
    private final static MABElement element = new SubjectOther();
    
    private SubjectOther() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
