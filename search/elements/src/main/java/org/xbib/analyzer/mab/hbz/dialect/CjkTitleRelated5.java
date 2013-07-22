package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class CjkTitleRelated5 extends MABElement {
    
    private final static MABElement element = new CjkTitleRelated5();
    
    private CjkTitleRelated5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
