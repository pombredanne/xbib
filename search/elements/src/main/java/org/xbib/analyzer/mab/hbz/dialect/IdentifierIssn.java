package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierISSN extends MABElement {
    
    private final static MABElement element = new IdentifierISSN();
    
    private IdentifierISSN() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
