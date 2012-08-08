package org.xbib.analyzer.marc;

import org.xbib.analyzer.marc.addons.MABElement;


public class TitleAlternative extends MABElement {
    
    private final static MABElement element = new TitleAlternative();
    
    private TitleAlternative() {
    }
    
    
    public static MABElement getInstance() {
        return element;
    }
}
