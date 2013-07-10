package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class PersonName extends MABElement {
    
    private final static MABElement element = new PersonName();
    
    private PersonName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
