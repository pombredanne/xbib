package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TypeMediaElectronic extends MABElement {
    
    private final static MABElement element = new TypeMediaElectronic();
    
    private TypeMediaElectronic() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
