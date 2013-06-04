package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ConferenceDate extends MABElement {
    
    private final static MABElement element = new ConferenceDate();
    
    private ConferenceDate() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
