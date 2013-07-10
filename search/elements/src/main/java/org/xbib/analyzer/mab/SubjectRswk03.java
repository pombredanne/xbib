package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk03 extends MABElement {
    
    private final static MABElement element = new SubjectRswk03();
    
    private SubjectRswk03() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
