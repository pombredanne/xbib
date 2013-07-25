package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierDNB extends MABElement {
    
    private final static MABElement element = new IdentifierDNB();
    
    private IdentifierDNB() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
