package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitlePart5 extends MABElement {
    
    private final static MABElement element = new TitlePart5();
    
    private TitlePart5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
