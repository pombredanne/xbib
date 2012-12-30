package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class SourceTitle extends MABElement {
    
    private final static MABElement element = new SourceTitle();
    
    private SourceTitle() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public SourceTitle build(MABBuilder b, FieldCollection key, String value) {
       // b.context().getResource(b.context().resource(), TITLE).add(XBIB_TITLE, value);
        return this;
    }

}
