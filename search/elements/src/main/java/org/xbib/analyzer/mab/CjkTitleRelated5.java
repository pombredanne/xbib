package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkTitleRelated5 extends MABElement {
    
    private final static MABElement element = new CjkTitleRelated5();
    
    private CjkTitleRelated5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
