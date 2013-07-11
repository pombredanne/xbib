package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleAlternative2 extends MABElement {
    
    private final static MABElement element = new TitleAlternative2();
    
    private TitleAlternative2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
