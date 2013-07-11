package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkTitleRelated1 extends MABElement {
    
    private final static MABElement element = new CjkTitleRelated1();
    
    private CjkTitleRelated1() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
