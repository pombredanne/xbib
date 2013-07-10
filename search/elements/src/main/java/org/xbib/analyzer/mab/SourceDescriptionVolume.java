package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceDescriptionVolume extends MABElement {
    
    private final static MABElement element = new SourceDescriptionVolume();
    
    private SourceDescriptionVolume() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
