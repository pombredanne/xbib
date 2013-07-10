package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class PersonIdentifier extends MABElement {
    
    private final static MABElement element = new PersonIdentifier();
    
    private PersonIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
