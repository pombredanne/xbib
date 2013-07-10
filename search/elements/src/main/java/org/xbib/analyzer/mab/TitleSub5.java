package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleSub5 extends MABElement {
    
    private final static MABElement element = new TitleSub5();
    
    private TitleSub5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
