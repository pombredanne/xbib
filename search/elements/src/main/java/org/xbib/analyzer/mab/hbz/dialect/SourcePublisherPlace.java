package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourcePublisherPlace extends MABElement {
    
    private final static MABElement element = new SourcePublisherPlace();
    
    private SourcePublisherPlace() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
