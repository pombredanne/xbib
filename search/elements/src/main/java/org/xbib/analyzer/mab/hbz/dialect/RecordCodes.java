package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.FieldCollection;

import java.util.Map;

public class RecordCodes extends MABElement {

    private final static MABElement element = new RecordCodes();

    private RecordCodes() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public boolean fields(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder,
                       FieldCollection fields, String value) {
        if (value == null) {
            return false;
        }
        Map<String,Object> codes = (Map<String,Object>)getSettings().get("codes");
        String data = value;
        String predicate = (String)codes.get("_predicate");
        if (predicate == null) {
            predicate = this.getClass().getSimpleName();
        }
        for (int i = 0; i < data.length(); i++) {
            Map<String,Object> q = (Map<String,Object>)codes.get(Integer.toString(i));
            if (q != null) {
                String code = (String)q.get(data.substring(i,i+1));
                builder.context().resource().add(predicate, code);
            }
        }
        return true; // done!
    }
}
