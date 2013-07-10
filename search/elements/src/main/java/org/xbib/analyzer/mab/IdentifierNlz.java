package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierNlz extends MABElement {
    
    private final static MABElement element = new IdentifierNlz();
    
    private IdentifierNlz() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
