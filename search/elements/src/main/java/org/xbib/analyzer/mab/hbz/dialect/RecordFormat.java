package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.FieldCollection;

public class RecordFormat extends MABElement {
    
    private final static MABElement element = new RecordFormat();
    
    private RecordFormat() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
    @Override
    public boolean fields(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder,
                       FieldCollection fields, String value) {
        builder.context().format(value.trim());
        return true;
    }
}
