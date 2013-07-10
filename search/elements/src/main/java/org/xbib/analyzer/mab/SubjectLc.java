package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectLc extends MABElement {
    
    private final static MABElement element = new SubjectLc();
    
    private SubjectLc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
