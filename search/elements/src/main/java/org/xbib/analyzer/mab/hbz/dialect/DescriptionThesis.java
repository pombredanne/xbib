package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class DescriptionThesis extends MABElement {
    
    private final static MABElement element = new DescriptionThesis();
    
    private DescriptionThesis() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
