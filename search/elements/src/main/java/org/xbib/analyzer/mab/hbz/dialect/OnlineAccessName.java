package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class OnlineAccessName extends MABElement {
    
    private final static MABElement element = new OnlineAccessName();
    
    private OnlineAccessName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
