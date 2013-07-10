package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectNdlc extends MABElement {
    
    private final static MABElement element = new SubjectNdlc();
    
    private SubjectNdlc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
