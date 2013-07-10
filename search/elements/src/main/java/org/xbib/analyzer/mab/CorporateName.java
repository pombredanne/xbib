package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CorporateName extends MABElement {
    
    private final static MABElement element = new CorporateName();
    
    private CorporateName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
