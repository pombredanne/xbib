package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleSub1 extends MABElement {
    
    private final static MABElement element = new TitleSub1();
    
    private TitleSub1() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
