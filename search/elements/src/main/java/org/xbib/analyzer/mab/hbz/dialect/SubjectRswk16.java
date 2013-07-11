package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk16 extends MABElement {
    
    private final static MABElement element = new SubjectRswk16();
    
    private SubjectRswk16() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
