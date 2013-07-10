package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectGhbs extends MABElement {
    
    private final static MABElement element = new SubjectGhbs();
    
    private SubjectGhbs() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
