package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkDateIssued extends MABElement {
    
    private final static MABElement element = new CjkDateIssued();
    
    private CjkDateIssued() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
