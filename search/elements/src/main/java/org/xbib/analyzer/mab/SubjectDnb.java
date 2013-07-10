package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectDnb extends MABElement {
    
    private final static MABElement element = new SubjectDnb();
    
    private SubjectDnb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
