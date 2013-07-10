package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk19 extends MABElement {
    
    private final static MABElement element = new SubjectRswk19();
    
    private SubjectRswk19() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
