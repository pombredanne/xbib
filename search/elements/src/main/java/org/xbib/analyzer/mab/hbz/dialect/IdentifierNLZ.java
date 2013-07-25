package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierNLZ extends MABElement {
    
    private final static MABElement element = new IdentifierNLZ();
    
    private IdentifierNLZ() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
