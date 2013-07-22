package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class CjkTitleSub extends MABElement {
    
    private final static MABElement element = new CjkTitleSub();
    
    private CjkTitleSub() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
