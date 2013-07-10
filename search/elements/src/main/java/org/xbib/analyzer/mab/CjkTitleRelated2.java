package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkTitleRelated2 extends MABElement {
    
    private final static MABElement element = new CjkTitleRelated2();
    
    private CjkTitleRelated2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
