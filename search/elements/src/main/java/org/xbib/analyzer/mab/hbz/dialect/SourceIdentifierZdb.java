package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceIdentifierZdb extends MABElement {
    
    private final static MABElement element = new SourceIdentifierZdb();
    
    private SourceIdentifierZdb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
