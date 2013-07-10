package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class PersonBio extends MABElement {
    
    private final static MABElement element = new PersonBio();
    
    private PersonBio() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
