package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;


public class TypeMediaElectronicSequence extends MABElement {
    
    private final static MABElement element = new TypeMediaElectronicSequence();
    
    private TypeMediaElectronicSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
