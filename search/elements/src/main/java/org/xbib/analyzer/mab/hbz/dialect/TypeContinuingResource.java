package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TypeContinuingResource extends MABElement {
    
    private final static MABElement element = new TypeContinuingResource();
    
    private TypeContinuingResource() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
