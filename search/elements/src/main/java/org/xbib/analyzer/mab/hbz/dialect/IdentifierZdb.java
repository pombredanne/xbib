package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierZdb extends MABElement {
    
    private final static MABElement element = new IdentifierZdb();
    
    private IdentifierZdb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
