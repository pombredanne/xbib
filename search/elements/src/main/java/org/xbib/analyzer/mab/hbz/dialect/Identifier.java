package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Identifier extends MABElement {

    private final static MABElement element = new Identifier();

    private Identifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
