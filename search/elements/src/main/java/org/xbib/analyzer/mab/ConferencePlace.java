package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class ConferencePlace extends MABElement {
    
    private final static MABElement element = new ConferencePlace();
    
    private ConferencePlace() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
