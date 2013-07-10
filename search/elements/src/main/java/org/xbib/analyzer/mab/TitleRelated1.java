package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleRelated1 extends MABElement {
    
    private final static MABElement element = new TitleRelated1();
    
    private TitleRelated1() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
