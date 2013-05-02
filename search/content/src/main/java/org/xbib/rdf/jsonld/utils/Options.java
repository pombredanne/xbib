package org.xbib.rdf.jsonld.utils;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Options {
    private String base = null;
    private  Boolean strict = null;
    public Boolean graph = null;
    public Boolean optimize = null;
    public Map<String, Object> optimizeCtx = null;
    public Boolean embed = null;
    public Boolean explicit = null;
    public Boolean omitDefault = null;
    public Boolean collate = null;
    public Boolean useRdfType = null;
    public Boolean useNativeTypes = null;

    private Set<String> ignoredKeys = new HashSet();

    // custom option to give to expand and compact which will generate @id's for elements that don't
    // have a specific @id
    public Boolean addBlankNodeIDs = false;

    public Options() {
        this.base = "";
        this.strict = true;
    }

    public Options(String base) {
        this.base = base;
        this.strict = true;
    }

    public Options(String base, Boolean strict) {
        this.base = base;
        this.strict = strict;
    }

    /**
     * Tells the processor to skip over the key specified by "key" any time it encounters it. Objects under this key will not be manipulated by any of the
     * processor functions and no triples will be created using it.
     *
     * @param key
     *            The name of the key this processor should ignore.
     */
    public Options ignoreKey(String key) {
        ignoredKeys.add(key);
        return this;
    }

    public Boolean isIgnored(String key) {
        return ignoredKeys.contains(key);
    }

    public Options setBase(String base) {
        this.base = base;
        return this;
    }

    public String getBase() {
        return base;
    }

    public Options setStrict(Boolean strict) {
        this.strict = strict;
        return this;
    }

    public Boolean getStrict() {
        return strict;
    }


    public Options clone() {
        Options rval = new Options(base);
        rval.strict = strict;
        rval.graph = graph;
        rval.optimize = optimize;
        rval.optimizeCtx = (Map<String, Object>) JSONLDUtils.clone(optimizeCtx);
        rval.embed = embed;
        rval.explicit = explicit;
        rval.omitDefault = omitDefault;
        rval.collate = collate;
        rval.useNativeTypes = useNativeTypes;
        rval.useRdfType = useRdfType;
        rval.addBlankNodeIDs = addBlankNodeIDs;
        for (String key: ignoredKeys) {
            rval.ignoreKey(key);
        }
        return rval;
    }
}
