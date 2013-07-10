package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Collection extends MABElement {
    
    private final static MABElement element = new Collection();
    
    private Collection() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
