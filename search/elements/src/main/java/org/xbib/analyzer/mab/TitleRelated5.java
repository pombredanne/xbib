package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleRelated5 extends MABElement {
    
    private final static MABElement element = new TitleRelated5();
    
    private TitleRelated5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
