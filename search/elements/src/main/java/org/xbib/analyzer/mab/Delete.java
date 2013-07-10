package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Delete extends MABElement {
    
    private final static MABElement element = new Delete();
    
    private Delete() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
