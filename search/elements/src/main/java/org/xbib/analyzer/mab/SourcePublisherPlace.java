package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourcePublisherPlace extends MABElement {
    
    private final static MABElement element = new SourcePublisherPlace();
    
    private SourcePublisherPlace() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
