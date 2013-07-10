package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CorporateIdentifier extends MABElement {
    
    private final static MABElement element = new CorporateIdentifier();
    
    private CorporateIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
