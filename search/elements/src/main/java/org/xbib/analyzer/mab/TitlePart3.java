package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitlePart3 extends MABElement {
    
    private final static MABElement element = new TitlePart3();
    
    private TitlePart3() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
