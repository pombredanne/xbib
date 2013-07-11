package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ItemShelfmark extends MABElement {
    
    private final static MABElement element = new ItemShelfmark();
    
    private ItemShelfmark() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
