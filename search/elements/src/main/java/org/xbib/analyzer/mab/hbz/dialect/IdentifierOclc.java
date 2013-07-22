package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierOCLC extends MABElement {
    
    private final static MABElement element = new IdentifierOCLC();
    
    private IdentifierOCLC() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
