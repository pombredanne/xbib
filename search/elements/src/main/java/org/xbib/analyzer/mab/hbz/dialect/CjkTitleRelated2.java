package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkTitleRelated2 extends MABElement {
    
    private final static MABElement element = new CjkTitleRelated2();
    
    private CjkTitleRelated2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
