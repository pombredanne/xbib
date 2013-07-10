package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class PersonAlternative extends MABElement {
    
    private final static MABElement element = new PersonAlternative();
    
    private PersonAlternative() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
