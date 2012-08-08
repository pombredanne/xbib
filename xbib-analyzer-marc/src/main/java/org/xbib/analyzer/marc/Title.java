package org.xbib.analyzer.marc;

import org.xbib.analyzer.marc.addons.MABBuilder;
import org.xbib.analyzer.marc.addons.MABElement;
import org.xbib.marc.FieldList;

public class Title extends MABElement {
    
    private final static MABElement element = new Title();
    
    private Title() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldList key, String value) {
        b.context().getResource(b.context().resource(), TITLE).addProperty(XBIB_TITLE, value);
    }

}
