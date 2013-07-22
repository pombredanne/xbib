package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.FieldCollection;

public class Publisher extends MABElement {
    
    private final static MABElement element = new Publisher();
    
    private Publisher() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
