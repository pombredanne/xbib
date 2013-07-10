package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitlePart4 extends MABElement {
    
    private final static MABElement element = new TitlePart4();
    
    private TitlePart4() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
