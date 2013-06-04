package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkTitleSub extends MABElement {
    
    private final static MABElement element = new CjkTitleSub();
    
    private CjkTitleSub() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
