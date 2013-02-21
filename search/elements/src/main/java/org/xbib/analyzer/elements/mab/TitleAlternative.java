package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;


public class TitleAlternative extends MABElement {
    
    private final static MABElement element = new TitleAlternative();
    
    private TitleAlternative() {
    }
    
    
    public static MABElement getInstance() {
        return element;
    }
}
