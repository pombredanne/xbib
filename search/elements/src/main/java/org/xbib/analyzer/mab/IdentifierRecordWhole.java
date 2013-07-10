package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierRecordWhole extends MABElement {
    
    private final static MABElement element = new IdentifierRecordWhole();
    
    private IdentifierRecordWhole() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
