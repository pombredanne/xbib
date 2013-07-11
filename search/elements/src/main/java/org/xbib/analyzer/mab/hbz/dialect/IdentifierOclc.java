package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierOclc extends MABElement {
    
    private final static MABElement element = new IdentifierOclc();
    
    private IdentifierOclc() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
