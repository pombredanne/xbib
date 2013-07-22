package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class DescriptionPublication extends MABElement {
    
    private final static MABElement element = new DescriptionPublication();
    
    private DescriptionPublication() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
