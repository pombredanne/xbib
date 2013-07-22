package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierISBN extends MABElement {
    
    private final static MABElement element = new IdentifierISBN();
    
    private IdentifierISBN() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
