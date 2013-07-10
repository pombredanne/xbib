package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkTitleUniform extends MABElement {
    
    private final static MABElement element = new CjkTitleUniform();
    
    private CjkTitleUniform() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
