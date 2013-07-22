package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierEKZ extends MABElement {

    private final static MABElement element = new IdentifierEKZ();

    private IdentifierEKZ() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
