package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleRelated2 extends MABElement {
    
    private final static MABElement element = new TitleRelated2();
    
    private TitleRelated2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
