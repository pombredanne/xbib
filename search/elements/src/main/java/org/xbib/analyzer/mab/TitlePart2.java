package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitlePart2 extends MABElement {
    
    private final static MABElement element = new TitlePart2();
    
    private TitlePart2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
