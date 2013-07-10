package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class GeoAlternativeSequence extends MABElement {
    
    private final static MABElement element = new GeoAlternativeSequence();
    
    private GeoAlternativeSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
