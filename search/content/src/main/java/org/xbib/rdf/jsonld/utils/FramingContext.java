package org.xbib.rdf.jsonld.utils;


import java.util.Map;

public class FramingContext {
    public Map<String,Object> embeds = null;
    public Map<String,Object> graphs = null;
    public Map<String,Object> subjects = null;
    public Boolean embed = true;
    public Boolean explicit = false;
    public Boolean omit = false;
    public Options options;

    public FramingContext(Options opts) {
        this.options = opts;
    }
}