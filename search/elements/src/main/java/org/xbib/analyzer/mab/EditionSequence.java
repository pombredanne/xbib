package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class EditionSequence extends MABElement {
    
    private final static MABElement element = new EditionSequence();
    
    private EditionSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
