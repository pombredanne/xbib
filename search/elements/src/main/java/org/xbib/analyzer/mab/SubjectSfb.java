package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectSfb extends MABElement {
    
    private final static MABElement element = new SubjectSfb();
    
    private SubjectSfb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
