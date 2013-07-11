package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkEdition extends MABElement {
    
    private final static MABElement element = new CjkEdition();
    
    private CjkEdition() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
