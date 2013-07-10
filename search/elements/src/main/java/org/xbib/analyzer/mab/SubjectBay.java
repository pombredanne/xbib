package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectBay extends MABElement {
    
    private final static MABElement element = new SubjectBay();
    
    private SubjectBay() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
