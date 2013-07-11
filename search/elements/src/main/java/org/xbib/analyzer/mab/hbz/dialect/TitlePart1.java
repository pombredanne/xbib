package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitlePart1 extends MABElement {
    
    private final static MABElement element = new TitlePart1();
    
    private TitlePart1() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
