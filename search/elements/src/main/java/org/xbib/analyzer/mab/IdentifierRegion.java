package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierRegion extends MABElement {
    
    private final static MABElement element = new IdentifierRegion();
    
    private IdentifierRegion() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
