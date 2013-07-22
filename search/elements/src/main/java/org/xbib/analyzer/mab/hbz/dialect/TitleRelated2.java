package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleRelated2 extends Title {

    private final static MABElement element = new TitleRelated2();

    private TitleRelated2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
