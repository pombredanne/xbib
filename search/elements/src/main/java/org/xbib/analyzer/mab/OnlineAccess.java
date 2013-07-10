package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class OnlineAccess extends MABElement {
    
    private final static MABElement element = new OnlineAccess();
    
    private OnlineAccess() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
