package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class TitlePart2 extends MABElement {
    
    private final static MABElement element = new TitlePart2();
    
    private TitlePart2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public TitlePart2 build(MABBuilder b, FieldCollection key, String value) {
        //b.context().getResource(b.context().resource(), TITLE).add(XBIB_TITLE, value);
        return this;
    }

}
