package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleRelated4 extends Title {

    private final static MABElement element = new TitleRelated4();

    private TitleRelated4() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
