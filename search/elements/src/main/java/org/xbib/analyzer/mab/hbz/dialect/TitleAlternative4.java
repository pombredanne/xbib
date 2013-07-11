package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleAlternative4 extends MABElement {
    
    private final static MABElement element = new TitleAlternative4();
    
    private TitleAlternative4() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
