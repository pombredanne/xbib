package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkDescription extends MABElement {
    
    private final static MABElement element = new CjkDescription();
    
    private CjkDescription() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
