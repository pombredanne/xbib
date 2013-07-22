package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleAlternative extends Title {

    private final static MABElement element = new TitleAlternative();

    private TitleAlternative() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
