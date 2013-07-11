package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionPrecedingEntry extends MABElement {
    
    private final static MABElement element = new DescriptionPrecedingEntry();
    
    private DescriptionPrecedingEntry() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
