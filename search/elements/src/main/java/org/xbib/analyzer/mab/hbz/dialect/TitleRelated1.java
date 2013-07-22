package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleRelated1 extends Title {
    
    private final static MABElement element = new TitleRelated1();
    
    private TitleRelated1() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
