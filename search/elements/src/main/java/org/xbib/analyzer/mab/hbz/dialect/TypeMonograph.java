package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.FieldCollection;

import java.util.Map;

public class TypeMonograph extends MABElement {
    
    private final static MABElement element = new TypeMonograph();
    
    TypeMonograph() {
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
        String data = value;
        Map<String,Object> codes = (Map<String,Object>)getSettings().get("codes");
        if (codes == null) {
            throw new IllegalStateException("no codes section for " + fields);
        }
        String predicate = (String)codes.get("_predicate");
        if (predicate == null) {
            predicate = this.getClass().getSimpleName();
        }
        for (int i = 0; i < data.length(); i++) {
            Map<String,Object> q = (Map<String,Object>)codes.get(Integer.toString(i));
            if (q != null) {
                String code = (String)q.get(data.substring(i,i+1));
                if (code == null && (i + 1 < data.length())) {
                    // two letters?
                    code = (String)q.get( data.substring(i,i+2));
                }
                builder.context().resource().add(predicate, code);
            }
        }
        return true; // done!
    }
}
