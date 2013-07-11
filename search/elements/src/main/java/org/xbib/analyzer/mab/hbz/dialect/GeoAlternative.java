package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class GeoAlternative extends MABElement {
    
    private final static MABElement element = new GeoAlternative();
    
    private GeoAlternative() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
