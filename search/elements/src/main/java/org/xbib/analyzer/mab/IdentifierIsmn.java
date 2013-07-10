package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierIsmn extends MABElement {
    
    private final static MABElement element = new IdentifierIsmn();
    
    private IdentifierIsmn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
