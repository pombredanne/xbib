package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class PersonRole extends MABElement {
    
    private final static MABElement element = new PersonRole();
    
    private PersonRole() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
