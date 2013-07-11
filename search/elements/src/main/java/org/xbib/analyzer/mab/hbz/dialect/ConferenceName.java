package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ConferenceName extends MABElement {
    
    private final static MABElement element = new ConferenceName();
    
    private ConferenceName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
