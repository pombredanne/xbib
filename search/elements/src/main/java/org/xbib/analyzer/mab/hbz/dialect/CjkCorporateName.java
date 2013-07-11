package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class CjkCorporateName extends MABElement {
    
    private final static MABElement element = new CjkCorporateName();
    
    private CjkCorporateName() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
