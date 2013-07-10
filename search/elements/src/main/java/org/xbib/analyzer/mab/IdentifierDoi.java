package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierDoi extends MABElement {
    
    private final static MABElement element = new IdentifierDoi();
    
    private IdentifierDoi() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
