package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk08 extends MABElement {
    
    private final static MABElement element = new SubjectRswk08();
    
    private SubjectRswk08() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
