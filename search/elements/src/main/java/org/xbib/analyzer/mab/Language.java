package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Language extends MABElement {
    
    private final static MABElement element = new Language();
    
    private Language() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
