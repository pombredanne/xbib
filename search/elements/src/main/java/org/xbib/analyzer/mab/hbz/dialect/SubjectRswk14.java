package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk14 extends MABElement {
    
    private final static MABElement element = new SubjectRswk14();
    
    private SubjectRswk14() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
