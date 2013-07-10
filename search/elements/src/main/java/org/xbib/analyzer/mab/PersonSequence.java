package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class PersonSequence extends MABElement {
    
    private final static MABElement element = new PersonSequence();
    
    private PersonSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
