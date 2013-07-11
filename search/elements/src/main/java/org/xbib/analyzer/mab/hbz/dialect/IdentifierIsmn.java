package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierIsmn extends MABElement {
    
    private final static MABElement element = new IdentifierIsmn();
    
    private IdentifierIsmn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
