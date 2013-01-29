package org.xbib.sessionstorage;

import java.io.Serializable;
import java.util.HashMap;

public class SessionAttributes extends HashMap<String, Object> implements Serializable {

    public String getAsString(String key) {
        return getAsString(key, "");
    }

    public String getAsString(String key, String defaultValue) {
        return containsKey(key) ? get(key).toString() : defaultValue;
    }

    public Long getAsLong(String key) {
        return getAsLong(key, 0L);
    }
    
    public Long getAsLong(String key, Long defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        Object o = get(key);
        if (o instanceof Long) {
            return (Long) o;
        }
        return Long.parseLong(o.toString());
    }

    public Boolean getAsBoolean(String key) {
        return getAsBoolean(key, false);
    }
    
    public Boolean getAsBoolean(String key, Boolean defaultValue) {
        if (!containsKey(key)) {
            return defaultValue;
        }
        Object o = get(key);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return Boolean.parseBoolean(o.toString());
    }
}
