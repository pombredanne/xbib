package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierZKA extends MABElement {

    private final static MABElement element = new IdentifierZKA();

    private IdentifierZKA() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
