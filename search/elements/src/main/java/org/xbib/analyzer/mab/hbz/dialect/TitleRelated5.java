package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleRelated5 extends Title {

    private final static MABElement element = new TitleRelated5();

    private TitleRelated5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
