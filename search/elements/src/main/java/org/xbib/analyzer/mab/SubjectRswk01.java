package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk01 extends MABElement {
    
    private final static MABElement element = new SubjectRswk01();
    
    private SubjectRswk01() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
