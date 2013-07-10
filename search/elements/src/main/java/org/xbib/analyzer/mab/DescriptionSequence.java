package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionSequence extends MABElement {
    
    private final static MABElement element = new DescriptionSequence();
    
    private DescriptionSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
