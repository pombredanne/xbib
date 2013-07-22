package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleSuper extends Title {
    
    private final static MABElement element = new TitleSuper();
    
    private TitleSuper() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
