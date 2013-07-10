package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ItemStatus extends MABElement {
    
    private final static MABElement element = new ItemStatus();
    
    private ItemStatus() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
