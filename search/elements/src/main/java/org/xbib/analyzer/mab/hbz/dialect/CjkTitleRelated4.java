package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkTitleRelated4 extends MABElement {
    
    private final static MABElement element = new CjkTitleRelated4();
    
    private CjkTitleRelated4() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
