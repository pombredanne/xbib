package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierRecordWhole extends MABElement {
    
    private final static MABElement element = new IdentifierRecordWhole();
    
    private IdentifierRecordWhole() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
