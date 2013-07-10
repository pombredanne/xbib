package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleRelated4 extends MABElement {
    
    private final static MABElement element = new TitleRelated4();
    
    private TitleRelated4() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
