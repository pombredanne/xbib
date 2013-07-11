package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierRecord extends MABElement {
    
    private final static MABElement element = new IdentifierRecord();
    
    private IdentifierRecord() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
