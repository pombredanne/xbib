package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class FormatCarrierDigital extends MABElement {
    
    private final static MABElement element = new FormatCarrierDigital();
    
    private FormatCarrierDigital() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
