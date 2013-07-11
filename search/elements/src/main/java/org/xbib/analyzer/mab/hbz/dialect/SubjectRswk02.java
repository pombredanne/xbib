package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRswk02 extends MABElement {
    
    private final static MABElement element = new SubjectRswk02();
    
    private SubjectRswk02() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
