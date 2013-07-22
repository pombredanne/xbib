package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.iri.IRI;
import org.xbib.marc.FieldCollection;

public class RecordIdentifier extends MABElement {
    
    private final static MABElement element = new RecordIdentifier();
    
    private RecordIdentifier() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    /*@Override
    public void fields(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder,
                       FieldCollection fields, String value) {
        //builder.context().resource().id(IRI.builder().scheme("mab").host(value.trim()).build());
    }*/
}
