package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectPrecis extends MABElement {
    
    private final static MABElement element = new SubjectPrecis();
    
    private SubjectPrecis() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
