package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CorporateAlternativeSequence extends MABElement {
    
    private final static MABElement element = new CorporateAlternativeSequence();
    
    private CorporateAlternativeSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
