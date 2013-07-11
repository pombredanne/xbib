package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierDoi extends MABElement {
    
    private final static MABElement element = new IdentifierDoi();
    
    private IdentifierDoi() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
