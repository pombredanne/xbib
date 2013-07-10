package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk02 extends MABElement {
    
    private final static MABElement element = new SubjectRswk02();
    
    private SubjectRswk02() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
