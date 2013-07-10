package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionPublicationSequence extends MABElement {
    
    private final static MABElement element = new DescriptionPublicationSequence();
    
    private DescriptionPublicationSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
