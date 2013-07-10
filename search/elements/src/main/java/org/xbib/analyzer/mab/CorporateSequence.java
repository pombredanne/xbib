package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CorporateSequence extends MABElement {
    
    private final static MABElement element = new CorporateSequence();
    
    private CorporateSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
