package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Collection extends MABElement {
    
    private final static MABElement element = new Collection();
    
    private Collection() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
