package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierZdb extends MABElement {
    
    private final static MABElement element = new IdentifierZdb();
    
    private IdentifierZdb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
