package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class PersonAlternativeSequence extends MABElement {
    
    private final static MABElement element = new PersonAlternativeSequence();
    
    private PersonAlternativeSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
