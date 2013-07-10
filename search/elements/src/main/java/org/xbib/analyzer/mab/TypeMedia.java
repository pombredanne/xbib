package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TypeMedia extends MABElement {
    
    private final static MABElement element = new TypeMedia();
    
    private TypeMedia() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
