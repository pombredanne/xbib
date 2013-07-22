package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Description extends MABElement {
    
    private final static MABElement element = new Description();
    
    private Description() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
