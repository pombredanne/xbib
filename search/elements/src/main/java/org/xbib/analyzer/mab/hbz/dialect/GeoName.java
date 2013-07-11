package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class GeoName extends MABElement {
    
    private final static MABElement element = new GeoName();
    
    private GeoName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
