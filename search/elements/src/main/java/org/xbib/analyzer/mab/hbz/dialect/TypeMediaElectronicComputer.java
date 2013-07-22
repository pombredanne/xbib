package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TypeMediaElectronicComputer extends MABElement {

    private final static MABElement element = new TypeMediaElectronicComputer();

    private TypeMediaElectronicComputer() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
