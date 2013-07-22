package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierBNB extends MABElement {

    private final static MABElement element = new IdentifierBNB();

    private IdentifierBNB() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
