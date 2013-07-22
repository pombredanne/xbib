package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Date extends MABElement {
    
    private final static MABElement element = new Date();
    
    private Date() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
