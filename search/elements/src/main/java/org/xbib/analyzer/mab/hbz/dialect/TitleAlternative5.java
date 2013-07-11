package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleAlternative5 extends MABElement {
    
    private final static MABElement element = new TitleAlternative5();
    
    private TitleAlternative5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
