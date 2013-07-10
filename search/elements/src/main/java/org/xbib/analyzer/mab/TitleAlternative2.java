package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleAlternative2 extends MABElement {
    
    private final static MABElement element = new TitleAlternative2();
    
    private TitleAlternative2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
