package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleRelated extends MABElement {

    private final static MABElement element = new TitleRelated();

    private TitleRelated() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
