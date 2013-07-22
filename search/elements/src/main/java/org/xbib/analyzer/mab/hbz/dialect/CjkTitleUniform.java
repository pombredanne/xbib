package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class CjkTitleUniform extends MABElement {
    
    private final static MABElement element = new CjkTitleUniform();
    
    private CjkTitleUniform() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
