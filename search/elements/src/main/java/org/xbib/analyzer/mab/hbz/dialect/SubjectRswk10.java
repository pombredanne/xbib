package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk10 extends MABElement {
    
    private final static MABElement element = new SubjectRswk10();
    
    private SubjectRswk10() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
