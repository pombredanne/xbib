package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitlePart extends Title {
    
    private final static MABElement element = new TitlePart();
    
    private TitlePart() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
