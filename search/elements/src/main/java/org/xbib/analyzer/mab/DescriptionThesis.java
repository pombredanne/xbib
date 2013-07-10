package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionThesis extends MABElement {
    
    private final static MABElement element = new DescriptionThesis();
    
    private DescriptionThesis() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
