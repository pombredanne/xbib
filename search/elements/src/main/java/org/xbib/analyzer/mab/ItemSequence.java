package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ItemSequence extends MABElement {
    
    private final static MABElement element = new ItemSequence();
    
    private ItemSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
