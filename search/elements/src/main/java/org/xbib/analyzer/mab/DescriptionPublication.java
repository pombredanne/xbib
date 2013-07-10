package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionPublication extends MABElement {
    
    private final static MABElement element = new DescriptionPublication();
    
    private DescriptionPublication() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
