package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkTitleRelated3 extends MABElement {
    
    private final static MABElement element = new CjkTitleRelated3();
    
    private CjkTitleRelated3() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
