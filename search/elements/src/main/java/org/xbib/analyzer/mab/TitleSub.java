package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleSub extends MABElement {
    
    private final static MABElement element = new TitleSub();
    
    private TitleSub() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
