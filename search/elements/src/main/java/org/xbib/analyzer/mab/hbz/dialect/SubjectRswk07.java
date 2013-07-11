package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk07 extends MABElement {
    
    private final static MABElement element = new SubjectRswk07();
    
    private SubjectRswk07() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
