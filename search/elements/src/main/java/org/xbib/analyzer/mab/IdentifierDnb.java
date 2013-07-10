package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierDnb extends MABElement {
    
    private final static MABElement element = new IdentifierDnb();
    
    private IdentifierDnb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
