package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk15 extends MABElement {
    
    private final static MABElement element = new SubjectRswk15();
    
    private SubjectRswk15() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
