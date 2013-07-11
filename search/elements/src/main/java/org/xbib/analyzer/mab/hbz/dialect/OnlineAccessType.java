package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class OnlineAccessType extends MABElement {
    
    private final static MABElement element = new OnlineAccessType();
    
    private OnlineAccessType() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
