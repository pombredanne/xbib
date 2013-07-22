package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierRegion extends MABElement {
    
    private final static MABElement element = new IdentifierRegion();
    
    private IdentifierRegion() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
