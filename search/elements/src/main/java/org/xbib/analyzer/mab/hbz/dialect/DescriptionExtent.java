package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionExtent extends MABElement {
    
    private final static MABElement element = new DescriptionExtent();
    
    private DescriptionExtent() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
