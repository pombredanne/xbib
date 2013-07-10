package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class OnlineAccessLabel extends MABElement {
    
    private final static MABElement element = new OnlineAccessLabel();
    
    private OnlineAccessLabel() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
