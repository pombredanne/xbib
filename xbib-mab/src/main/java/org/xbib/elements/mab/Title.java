package org.xbib.elements.mab;

import org.xbib.marc.FieldDesignatorList;

public class Title extends MABElement {
    
    private final static MABElement element = new Title();
    
    private Title() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldDesignatorList key, String value) {
        b.context().getResource(b.context().resource(), TITLE).addProperty(XBIB_TITLE, value);
    }

}
