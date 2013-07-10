package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ItemCollection extends MABElement {
    
    private final static MABElement element = new ItemCollection();
    
    private ItemCollection() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
