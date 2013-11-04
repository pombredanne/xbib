package org.xbib.objectstorage.request;

import org.xbib.objectstorage.Parameter;
import org.xbib.objectstorage.Request;
import org.xbib.objectstorage.adapter.AbstractAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRequest implements Request, Parameter {

    protected final Map<String, Object> request = new HashMap<>();

    protected AbstractAdapter adapter;

    protected String container;

    protected String item;

    @Override
    public Request setContainer(String container) {
        this.container = container;
        return this;
    }

    @Override
    public AbstractAdapter getAdapter() {
        return adapter;
    }

    @Override
    public String getContainer() {
        return container;
    }

    @Override
    public Request setItem(String item) {
        this.item = item;
        return this;
    }

    @Override
    public String getItem() {
        return item;
    }

    @Override
    public Request addStringParameter(String key, String value) {
        request.put(key, value);
        return this;
    }

    @Override
    public Request addDateParameter(String key, Date value) {
        request.put(key, value);
        return this;
    }

    @Override
    public Request addLongParameter(String key, Long value) {
        request.put(key, value);
        return this;
    }

    @Override
    public Long getLongParameter(String key, Long defaultValue) {
        return (request.containsKey(key) && request.get(key) instanceof Long)
                ? (Long) request.get(key) : defaultValue;
    }

    @Override
    public String getStringParameter(String key, String defaultValue) {
        return (request.containsKey(key) && request.get(key) instanceof String)
                ? (String) request.get(key) : defaultValue;
    }

    @Override
    public Date getDateParameter(String key, Date defaultValue) {
        return (request.containsKey(key) && request.get(key) instanceof Date)
                ? (Date) request.get(key) : defaultValue;
    }

    @Override
    public Request setUser(String user) {
        request.put(USER_PARAMETER, user);
        return this;
    }

    @Override
    public String getUser() {
        return (String) request.get(USER_PARAMETER);
    }

    public String toString() {
        return "[Adapter=" + adapter +"] " +
                "[Container=" + container + "] " +
                request.toString();
    }
}
