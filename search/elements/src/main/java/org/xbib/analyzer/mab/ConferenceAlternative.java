package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ConferenceAlternative extends MABElement {
    
    private final static MABElement element = new ConferenceAlternative();
    
    private ConferenceAlternative() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
