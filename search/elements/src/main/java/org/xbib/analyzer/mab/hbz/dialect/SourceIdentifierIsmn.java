package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceIdentifierIsmn extends MABElement {
    
    private final static MABElement element = new SourceIdentifierIsmn();
    
    private SourceIdentifierIsmn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
