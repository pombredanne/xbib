package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleRelated3 extends Title {

    private final static MABElement element = new TitleRelated3();

    private TitleRelated3() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
