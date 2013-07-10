package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionThesisSequence extends MABElement {
    
    private final static MABElement element = new DescriptionThesisSequence();
    
    private DescriptionThesisSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
