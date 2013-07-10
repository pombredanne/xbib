package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectEpp extends MABElement {
    
    private final static MABElement element = new SubjectEpp();
    
    private SubjectEpp() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
