package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectEpp extends MABElement {
    
    private final static MABElement element = new SubjectEpp();
    
    private SubjectEpp() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
