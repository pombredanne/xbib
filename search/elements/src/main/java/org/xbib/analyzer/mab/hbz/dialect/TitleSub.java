package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleSub extends Title {
    
    private final static MABElement element = new TitleSub();
    
    private TitleSub() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
