package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkPublisherPlace extends MABElement {
    
    private final static MABElement element = new CjkPublisherPlace();
    
    private CjkPublisherPlace() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
