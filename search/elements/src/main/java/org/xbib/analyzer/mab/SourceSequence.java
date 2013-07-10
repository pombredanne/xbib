package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceSequence extends MABElement {
    
    private final static MABElement element = new SourceSequence();
    
    private SourceSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
