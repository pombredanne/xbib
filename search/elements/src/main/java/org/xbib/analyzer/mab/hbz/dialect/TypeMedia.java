package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TypeMedia extends MABElement {
    
    private final static MABElement element = new TypeMedia();
    
    private TypeMedia() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
