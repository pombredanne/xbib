package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class DescriptionTypeMediaElectronic extends MABElement {

    private final static MABElement element = new DescriptionTypeMediaElectronic();

    private DescriptionTypeMediaElectronic() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
