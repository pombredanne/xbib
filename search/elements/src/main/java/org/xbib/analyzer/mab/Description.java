package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Description extends MABElement {
    
    private final static MABElement element = new Description();
    
    private Description() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
