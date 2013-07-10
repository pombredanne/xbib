package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class GeoName extends MABElement {
    
    private final static MABElement element = new GeoName();
    
    private GeoName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
