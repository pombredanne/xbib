package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class PublisherName extends MABElement {
    
    private final static MABElement element = new PublisherName();
    
    private PublisherName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
