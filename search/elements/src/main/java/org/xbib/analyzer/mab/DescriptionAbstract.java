package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionAbstract extends MABElement {
    
    private final static MABElement element = new DescriptionAbstract();
    
    private DescriptionAbstract() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
