package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierBNF extends MABElement {

    private final static MABElement element = new IdentifierBNF();

    private IdentifierBNF() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
