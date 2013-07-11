package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CorporateAlternative extends MABElement {
    
    private final static MABElement element = new CorporateAlternative();
    
    private CorporateAlternative() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
