package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Geo extends MABElement {
    
    private final static MABElement element = new Geo();
    
    private Geo() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
