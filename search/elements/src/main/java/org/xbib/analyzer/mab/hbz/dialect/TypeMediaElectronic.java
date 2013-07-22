package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TypeMediaElectronic extends MABElement {
    
    private final static MABElement element = new TypeMediaElectronic();
    
    private TypeMediaElectronic() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
