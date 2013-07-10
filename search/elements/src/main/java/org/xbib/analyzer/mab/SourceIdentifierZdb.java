package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceIdentifierZdb extends MABElement {
    
    private final static MABElement element = new SourceIdentifierZdb();
    
    private SourceIdentifierZdb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
