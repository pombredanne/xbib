package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class OnlineAccess extends MABElement {
    
    private final static MABElement element = new OnlineAccess();
    
    private OnlineAccess() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
