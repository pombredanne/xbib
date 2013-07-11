package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierIsrn extends MABElement {
    
    private final static MABElement element = new IdentifierIsrn();
    
    private IdentifierIsrn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
