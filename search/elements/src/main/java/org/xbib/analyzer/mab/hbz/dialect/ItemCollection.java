package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class ItemCollection extends MABElement {
    
    private final static MABElement element = new ItemCollection();
    
    private ItemCollection() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
