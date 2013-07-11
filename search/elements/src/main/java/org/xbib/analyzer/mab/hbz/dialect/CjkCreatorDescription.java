package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkCreatorDescription extends MABElement {
    
    private final static MABElement element = new CjkCreatorDescription();
    
    private CjkCreatorDescription() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
