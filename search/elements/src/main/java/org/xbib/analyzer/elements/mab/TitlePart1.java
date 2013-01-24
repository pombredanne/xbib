package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class TitlePart1 extends MABElement {
    
    private final static MABElement element = new TitlePart1();
    
    private TitlePart1() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public TitlePart1 build(MABBuilder b, FieldCollection key, String value) {
        //b.context().getResource(b.context().resource(), TITLE).add(XBIB_TITLE, value);
        return this;
    }

}
