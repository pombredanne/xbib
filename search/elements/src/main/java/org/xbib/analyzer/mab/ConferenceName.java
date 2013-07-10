package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ConferenceName extends MABElement {
    
    private final static MABElement element = new ConferenceName();
    
    private ConferenceName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
