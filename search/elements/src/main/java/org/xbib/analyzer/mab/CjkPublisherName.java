package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkPublisherName extends MABElement {
    
    private final static MABElement element = new CjkPublisherName();
    
    private CjkPublisherName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
