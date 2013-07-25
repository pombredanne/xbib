package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierZDB extends MABElement {
    
    private final static MABElement element = new IdentifierZDB();
    
    private IdentifierZDB() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
