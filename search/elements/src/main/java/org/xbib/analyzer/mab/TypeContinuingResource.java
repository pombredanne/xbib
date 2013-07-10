package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TypeContinuingResource extends MABElement {
    
    private final static MABElement element = new TypeContinuingResource();
    
    private TypeContinuingResource() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
