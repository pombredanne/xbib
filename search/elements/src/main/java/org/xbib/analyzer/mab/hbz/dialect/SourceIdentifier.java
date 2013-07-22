package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceIdentifier extends MABElement {
    
    private final static MABElement element = new SourceIdentifier();
    
    private SourceIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
