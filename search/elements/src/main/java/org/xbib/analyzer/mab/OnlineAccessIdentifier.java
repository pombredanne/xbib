package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class OnlineAccessIdentifier extends MABElement {
    
    private final static MABElement element = new OnlineAccessIdentifier();
    
    private OnlineAccessIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
