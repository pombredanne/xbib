package org.xbib.elements;

import java.util.Map;

public class NullElement implements Element {

    @Override
    public Element setSettings(Map settings) {
        return this;
    }

    @Override
    public Map<String, Object> getSettings() {
        return null;
    }

    @Override
    public Element begin() {
        return this;
    }

    @Override
    public Element build(ElementBuilder builder, Object key, Object value) {
        return this;
    }

    @Override
    public Element end() {
        return this;
    }

    public String toString() {
        return "<null>";
    }
}
