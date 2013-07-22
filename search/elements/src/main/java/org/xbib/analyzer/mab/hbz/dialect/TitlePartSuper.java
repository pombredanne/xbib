package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitlePartSuper extends Title {
    
    private final static MABElement element = new TitlePartSuper();
    
    private TitlePartSuper() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
