package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ConferenceAlternativeSequence extends MABElement {
    
    private final static MABElement element = new ConferenceAlternativeSequence();
    
    private ConferenceAlternativeSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
