package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Delete extends MABElement {
    
    private final static MABElement element = new Delete();
    
    private Delete() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
