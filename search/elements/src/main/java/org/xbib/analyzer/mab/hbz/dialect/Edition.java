package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Edition extends MABElement {
    
    private final static MABElement element = new Edition();
    
    private Edition() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
