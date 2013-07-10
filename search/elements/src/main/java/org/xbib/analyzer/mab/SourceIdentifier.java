package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceIdentifier extends MABElement {
    
    private final static MABElement element = new SourceIdentifier();
    
    private SourceIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
