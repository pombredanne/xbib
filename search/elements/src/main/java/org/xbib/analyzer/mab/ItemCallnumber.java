package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ItemCallnumber extends MABElement {
    
    private final static MABElement element = new ItemCallnumber();
    
    private ItemCallnumber() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
