package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class FormatCarrierDigital extends MABElement {
    
    private final static MABElement element = new FormatCarrierDigital();
    
    private FormatCarrierDigital() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
