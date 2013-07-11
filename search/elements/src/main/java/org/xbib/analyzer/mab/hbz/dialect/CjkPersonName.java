package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkPersonName extends MABElement {
    
    private final static MABElement element = new CjkPersonName();
    
    private CjkPersonName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
