package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectNdlc extends MABElement {
    
    private final static MABElement element = new SubjectNdlc();
    
    private SubjectNdlc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
