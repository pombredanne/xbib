package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceDateIssued extends MABElement {
    
    private final static MABElement element = new SourceDateIssued();
    
    private SourceDateIssued() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
