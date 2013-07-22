package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TypeMediaElectronicPreservation extends MABElement {

    private final static MABElement element = new TypeMediaElectronicPreservation();

    private TypeMediaElectronicPreservation() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
