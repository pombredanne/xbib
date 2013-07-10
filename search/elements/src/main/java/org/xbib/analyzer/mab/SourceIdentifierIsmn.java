package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceIdentifierIsmn extends MABElement {
    
    private final static MABElement element = new SourceIdentifierIsmn();
    
    private SourceIdentifierIsmn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
