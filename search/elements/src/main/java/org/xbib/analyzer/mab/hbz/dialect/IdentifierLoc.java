package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierLoc extends MABElement {
    
    private final static MABElement element = new IdentifierLoc();
    
    private IdentifierLoc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
