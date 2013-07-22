package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceDateIssued extends MABElement {
    
    private final static MABElement element = new SourceDateIssued();
    
    private SourceDateIssued() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
