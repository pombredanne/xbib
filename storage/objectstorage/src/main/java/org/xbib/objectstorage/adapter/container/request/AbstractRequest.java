package org.xbib.objectstorage.adapter.container.request;

import org.xbib.objectstorage.ObjectStorageParameter;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.objectstorage.adapter.AbstractAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRequest implements
        ObjectStorageRequest, ObjectStorageParameter {
    protected final Map<String, Object> request = new HashMap<>();
    protected AbstractAdapter adapter;
    protected String container;
    protected String item;

    @Override
    public ObjectStorageRequest setContainer(String container) {
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
    public ObjectStorageRequest setItem(String item) {
        this.item = item;
        return this;
    }

    @Override
    public String getItem() {
        return item;
    }

    @Override
    public ObjectStorageRequest addStringParameter(String key, String value) {
        request.put(key, value);
        return this;
    }

    @Override
    public ObjectStorageRequest addDateParameter(String key, Date value) {
        request.put(key, value);
        return this;
    }

    @Override
    public ObjectStorageRequest addLongParameter(String key, Long value) {
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
    public ObjectStorageRequest setUser(String user) {
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
