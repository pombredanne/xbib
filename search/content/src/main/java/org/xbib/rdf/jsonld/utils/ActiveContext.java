package org.xbib.rdf.jsonld.utils;

import org.xbib.rdf.jsonld.JsonLd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A helper class which still stores all the values in a map
 * but gives member variables easily access certain keys
 *
 */
public class ActiveContext extends HashMap<String, Object> implements JsonLd {
    public Map<String, Object> mappings;
    public Map<String, List<String>> keywords;

    public ActiveContext() {
        super();
        init();
    }

    private void init() {
        if (!this.containsKey("mappings")) {
            this.put("mappings", new HashMap<String, Object>());
        }
        if (!this.containsKey("keywords")) {
            this.put("keywords", new HashMap<String, List<String>>() {
                {
                    put(JSONLD_CONTEXT, new ArrayList<String>());
                    put(JSONLD_CONTAINER, new ArrayList<String>());
                    put(JSONLD_DEFAULT, new ArrayList<String>());
                    put(JSONLD_EMBED, new ArrayList<String>());
                    put(JSONLD_EXPLICIT, new ArrayList<String>());
                    put(JSONLD_GRAPH, new ArrayList<String>());
                    put(JSONLD_ID, new ArrayList<String>());
                    put(JSONLD_LANGUAGE, new ArrayList<String>());
                    put(JSONLD_LIST, new ArrayList<String>());
                    put(JSONLD_OMITDEFAULT, new ArrayList<String>());
                    put(JSONLD_PRESERVE, new ArrayList<String>());
                    put(JSONLD_SET, new ArrayList<String>());
                    put(JSONLD_TYPE, new ArrayList<String>());
                    put(JSONLD_VALUE, new ArrayList<String>());
                    put(JSONLD_VOCAB, new ArrayList<String>());
                    // add ignored keywords
                        /*
                        for (String key : ignoredKeywords) {
                            put(keyword, new ArrayList<String>());
                        }
                        */
                }
            });
        }
        mappings = (Map<String, Object>) this.get("mappings");
        keywords = (Map<String, List<String>>) this.get("keywords");
    }

    public Object getContextValue(String key, String type) {
        if (key == null) {
            return null;
        }
        Object rval = null;
        if (JSONLD_LANGUAGE.equals(type) && this.containsKey(type)) {
            rval = this.get(type);
        }

        if (mappings.containsKey(key)) {
            Map<String, Object> entry = (Map<String, Object>) this.mappings.get(key);

            if (type == null) {
                rval = entry;
            } else if (entry.containsKey(type)) {
                rval = entry.get(type);
            }
        }

        return rval;
    }

}
