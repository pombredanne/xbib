package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class Title extends MABElement {
    
    private final static MABElement element = new Title();
    
    private Title() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public Title build(MABBuilder b, FieldCollection key, String value) {
        b.context().resource().add(XBIB_TITLE, value);
        return this;
    }

}
