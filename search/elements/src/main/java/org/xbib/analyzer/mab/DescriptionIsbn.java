package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionIsbn extends MABElement {
    
    private final static MABElement element = new DescriptionIsbn();
    
    private DescriptionIsbn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
