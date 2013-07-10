package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CreatorDescription extends MABElement {
    
    private final static MABElement element = new CreatorDescription();
    
    private CreatorDescription() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
