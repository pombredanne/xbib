package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class ItemLibraryIdentifier extends MABElement {
    
    private final static MABElement element = new ItemLibraryIdentifier();
    
    private ItemLibraryIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
