package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class SourceTitleWhole extends MABElement {
    
    private final static MABElement element = new SourceTitleWhole();
    
    private SourceTitleWhole() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public SourceTitleWhole build(MABBuilder b, FieldCollection key, String value) {
       // b.context().getResource(b.context().resource(), TITLE).add(XBIB_TITLE, value);
        return this;
    }

}
