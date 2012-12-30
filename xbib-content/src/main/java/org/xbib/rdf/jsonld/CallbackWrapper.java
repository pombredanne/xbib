package org.xbib.rdf.jsonld;

import java.util.Map;

public interface CallbackWrapper {

        void callback(Map<String, Object> statement);

        void processIgnored(Object parent, String parentId, String prop, Object object);
    }