package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitlePart extends MABElement {
    
    private final static MABElement element = new TitlePart();
    
    private TitlePart() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
