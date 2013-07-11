package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class PersonName extends MABElement {
    
    private final static MABElement element = new PersonName();
    
    private PersonName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
