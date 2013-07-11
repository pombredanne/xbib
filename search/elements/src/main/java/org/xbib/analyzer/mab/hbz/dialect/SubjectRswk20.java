package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk20 extends MABElement {
    
    private final static MABElement element = new SubjectRswk20();
    
    private SubjectRswk20() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
