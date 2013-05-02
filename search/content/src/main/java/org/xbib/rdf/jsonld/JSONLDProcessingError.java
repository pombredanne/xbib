package org.xbib.rdf.jsonld;

import java.util.HashMap;
import java.util.Map;

public class JSONLDProcessingError extends Exception {

    String message;
    Map details;

    public JSONLDProcessingError(String string, Map<String, Object> details) {
        this. message = string;
        this.details = details;
    }

    public JSONLDProcessingError(String string) {
        message = string;
        details = new HashMap();
    }

    public JSONLDProcessingError setDetail(String string, Object val) {
        details.put(string, val);
        return this;
    }

}
