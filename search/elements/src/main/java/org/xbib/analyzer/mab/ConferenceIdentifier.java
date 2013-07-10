package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ConferenceIdentifier extends MABElement {
    
    private final static MABElement element = new ConferenceIdentifier();
    
    private ConferenceIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
