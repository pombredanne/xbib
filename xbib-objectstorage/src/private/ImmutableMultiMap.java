package org.xbib.filestorage.oauth;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.ws.rs.core.MultivaluedMap;

public class ImmutableMultiMap extends MultivaluedMapImpl {

    public static MultivaluedMap<String, String> newImmutableMultiMap(Map<String, List<String>> source) {
        if (source == null) {
            return ImmutableMultiMap.EMPTY;
        }
        return new ImmutableMultiMap(source);
    }
    public static final ImmutableMultiMap EMPTY = new ImmutableMultiMap();

    private ImmutableMultiMap() {
    }

    ImmutableMultiMap(Map<String, List<String>> source) {
        for (Map.Entry<String, List<String>> e : source.entrySet()) {
            super.put(e.getKey(), e.getValue() == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<String>(e.getValue())));
        }
    }

    @Override
    public List<String> put(String k, List<String> v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        return Collections.unmodifiableSet(super.entrySet());
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(super.keySet());
    }

    @Override
    public List<String> remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<String>> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<List<String>> values() {
        return Collections.unmodifiableCollection(super.values());
    }
}
