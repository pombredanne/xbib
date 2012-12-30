package org.xbib.rdf.jsonld;

import java.util.Map;

public class ToRDFCallback implements CallbackWrapper {

    private JSONLDTripleCallback cb;

    public ToRDFCallback(JSONLDTripleCallback cb) {
        this.cb = cb;
    }

    public void callback(Map<String, Object> statement) {
        if (statement == null) {
            return;
        }
        Map<String, Object> s = (Map<String, Object>) statement.get("subject");
        Map<String, Object> p = (Map<String, Object>) statement.get("property");
        Map<String, Object> o = (Map<String, Object>) statement.get("object");
        Map<String, Object> g = (Map<String, Object>) statement.get("name");

        String sub = (String) s.get("nominalValue");
        String pre = (String) p.get("nominalValue");
        String obj = (String) o.get("nominalValue");

        String graph = null;
        if (g != null) {
            graph = (String) g.get("nominalValue");
        }

        if (o.containsKey("datatype")
                && !JsonLd.XSD_STRING.equals(((Map<String, Object>) o.get("datatype")).get("nominalValue"))) {
            cb.triple(sub, pre, obj, (String) ((Map<String, Object>) o.get("datatype")).get("nominalValue"), null, graph);
        } else if (o.containsKey("language")) {
            cb.triple(sub, pre, obj, (String) null, (String) o.get("language"), graph);
        } else if ("LiteralNode".equals(o.get("interfaceName"))) {
            cb.triple(sub, pre, obj, null, null, graph);
        } else {
            cb.triple(sub, pre, obj, graph);
        }
    }

    @Override
    public void processIgnored(Object parent, String parentId, String prop, Object object) {
        cb.processIgnored(parent, parentId, prop, object);
    }
}