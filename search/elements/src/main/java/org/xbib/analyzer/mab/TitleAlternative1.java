package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleAlternative1 extends MABElement {
    
    private final static MABElement element = new TitleAlternative1();
    
    private TitleAlternative1() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
