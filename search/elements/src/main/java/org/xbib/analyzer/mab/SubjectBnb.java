package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectBnb extends MABElement {
    
    private final static MABElement element = new SubjectBnb();
    
    private SubjectBnb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
