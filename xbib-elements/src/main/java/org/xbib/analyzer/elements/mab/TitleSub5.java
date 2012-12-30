package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class TitleSub5 extends MABElement {
    
    private final static MABElement element = new TitleSub5();
    
    private TitleSub5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public TitleSub5 build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
