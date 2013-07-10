package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ConferenceSequence extends MABElement {
    
    private final static MABElement element = new ConferenceSequence();
    
    private ConferenceSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
