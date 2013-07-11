package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectIfz extends MABElement {
    
    private final static MABElement element = new SubjectIfz();
    
    private SubjectIfz() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
