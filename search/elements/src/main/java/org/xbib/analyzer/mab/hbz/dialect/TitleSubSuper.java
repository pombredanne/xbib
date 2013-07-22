package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleSubSuper extends Title {
    
    private final static MABElement element = new TitleSubSuper();
    
    private TitleSubSuper() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
