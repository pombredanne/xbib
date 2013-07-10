package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class OnlineAccessSequence extends MABElement {
    
    private final static MABElement element = new OnlineAccessSequence();
    
    private OnlineAccessSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
