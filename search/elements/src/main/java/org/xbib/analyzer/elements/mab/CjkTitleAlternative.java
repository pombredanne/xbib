package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkTitleAlternative extends MABElement {
    
    private final static MABElement element = new CjkTitleAlternative();
    
    private CjkTitleAlternative() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
