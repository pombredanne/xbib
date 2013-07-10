package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class FormatCarrierSequence extends MABElement {
    
    private final static MABElement element = new FormatCarrierSequence();
    
    private FormatCarrierSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
