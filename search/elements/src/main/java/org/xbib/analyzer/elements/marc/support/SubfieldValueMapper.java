package org.xbib.analyzer.elements.marc.support;

import java.util.Map;
import org.xbib.marc.Field;

public class SubfieldValueMapper {

    private SubfieldValueMapper() {
    }

    public static Map.Entry<String, Object> 
            map(Map subfields, final Field field) {
        String k = null;
        Object v = field.data();
        Object subfieldDef = subfields.get(field.subfieldId());
        if (subfieldDef instanceof Map) {
            // key/value mapping
            Map subfieldmap = (Map) subfieldDef;
            if (subfieldmap.containsKey(v)) {
                Object o = subfieldmap.get(v);
                if (o instanceof Map) {
                    Map.Entry<String, Object> me =
                            (Map.Entry<String, Object>) ((Map) o).entrySet().iterator().next();
                    k = me.getKey();
                    v = me.getValue();
                } else {
                    v = (String) o;
                }
            } else if (subfieldmap.containsKey("")) {
                k = (String) subfieldmap.get("");
            }
        } else {
            // new key (may be null to skip the value)
            k = (String)subfieldDef;
        }
        // create result map entry
        final String newKey = k;
        final Object newValue = v;
        final Map.Entry<String, Object> entry = new Map.Entry<String, Object>() {
            @Override
            public String getKey() {
                return newKey;
            }

            @Override
            public Object getValue() {
                return newValue;
            }

            @Override
            public Object setValue(Object value) {
               return null;
            }
        };
        return entry;
    }
}
