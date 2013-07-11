package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkSubjectOther extends MABElement {
    
    private final static MABElement element = new CjkSubjectOther();
    
    private CjkSubjectOther() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
