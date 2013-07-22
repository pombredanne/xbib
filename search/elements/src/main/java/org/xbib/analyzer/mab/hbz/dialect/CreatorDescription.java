package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class CreatorDescription extends MABElement {
    
    private final static MABElement element = new CreatorDescription();
    
    private CreatorDescription() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
