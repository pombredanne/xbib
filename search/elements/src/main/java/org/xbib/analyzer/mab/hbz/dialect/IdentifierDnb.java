package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierDnb extends MABElement {
    
    private final static MABElement element = new IdentifierDnb();
    
    private IdentifierDnb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
