package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class TitleUniformSuper extends Title {
    
    private final static MABElement element = new TitleUniformSuper();

    public static MABElement getInstance() {
        return element;
    }
}
