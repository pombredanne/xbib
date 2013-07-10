package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceIdentifierIsrn extends MABElement {
    
    private final static MABElement element = new SourceIdentifierIsrn();
    
    private SourceIdentifierIsrn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
