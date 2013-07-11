package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk17 extends MABElement {
    
    private final static MABElement element = new SubjectRswk17();
    
    private SubjectRswk17() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
