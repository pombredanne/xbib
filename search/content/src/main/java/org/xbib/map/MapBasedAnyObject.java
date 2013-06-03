/*
 *  Copyright 2011 BigData Mx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.xbib.map;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapBasedAnyObject implements AnyObject {

    private final Map map;

    private final String separator;

    private final ObjectMapper mapper = new ObjectMapper();

    public MapBasedAnyObject(Map map) {
        this(map, "\\.");
    }

    public MapBasedAnyObject(Map map, String separator) {
        this.map = new LinkedHashMap(map);
        this.separator = separator;
    }

    public Map map() {
        return map;
    }

    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=98379
    // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6302954
    <T> T get(String key) {
        return this.<T>get(map, key.split("\\."));
    }

    private <T> T get(Map inner, String[] key) {
        if (inner == null) {
            return null;
        }
        Object o = inner.get(key[0]);
        if (o instanceof List) {
            o = ((List)o).get(0);
        }
        return (T) (key.length > 1
                ? get((Map) o, Arrays.copyOfRange(key, 1, key.length)) : o);
    }

    <T> T getAll(String key) {
        return this.<T>getAll(map, key.split("\\."));
    }

    private <T> T getAll(Map inner, String[] key) {
        if (inner == null) {
            return null;
        }
        Object o = inner.get(key[0]);
        return (T) (key.length > 1
                ? getAll((Map) o, Arrays.copyOfRange(key, 1, key.length)) : o);
    }

    @Override
    public AnyObject getAnyObject(String key) {
        Object o = get(key);
        if (o == null) {
            return null;
        }
        if (o instanceof Map) {
            return new MapBasedAnyObject((Map) o);
        }
        return (MapBasedAnyObject) o;
    }

    @Override
    public <T> Iterable<T> getIterable(final String key) {
        final Object o = getAll(key);
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return new ArrayList<T>() {{ add((T)o); }};
        }
        if (!(o instanceof Iterable)) {
            return new ArrayList<T>() {{ add((T)o); }};
        }
        Iterable<T> iterable = (Iterable<T>)o;
        return (iterable == null) ? null : new AnyIterable(iterable);
    }

    @Override
    public Iterable<AnyTuple> getTuples() {
        return new AnyTupleIterable(map);
    }

    @Override
    public Long getLong(String key) {
        return (Long) get(key);
    }

    @Override
    public Integer getInteger(String key) {
        return (Integer) get(key);
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public Double getDouble(String key) {
        return (Double) get(key);
    }

    public Float getFloat(String key) {
        return (Float) get(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    private Object get(String key, Object defValue) {
        Object o = get(key);
        return (o != null) ? o : defValue;
    }

    public Long getLong(String key, Long defValue) {
        return (Long) get(key, defValue);
    }

    public Integer getInteger(String key, Integer defValue) {
        return (Integer) get(key, defValue);
    }

    public String getString(String key, String defValue) {
        return (String) get(key, defValue);
    }

    public Double getDouble(String key, Double defValue) {
        return (Double) get(key, defValue);
    }

    public Float getFloat(String key, Float defValue) {
        return (Float) get(key, defValue);
    }

    public Boolean getBoolean(String key, Boolean defValue) {
        return (Boolean) get(key, defValue);
    }

    public byte[] toJsonAsBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            mapper.writeValue(out, map);
            return out.toByteArray();
        } finally {
            out.close();
        }
    }

    public String toJson() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            mapper.writeValue(out, map);
            return out.toString();
        } finally {
            out.close();
        }
    }

    public String toString() {
        return map.toString();
    }

    private final static class AnyIterator implements Iterator {

        private final Iterator iterator;

        AnyIterator(Iterator iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Object next() {
            Object o = iterator.next();
            if (o instanceof Iterable) {
                return new AnyIterable((Iterable) o);
            }
            if (o instanceof Map) {
                return new MapBasedAnyObject((Map) o);
            }
            return o;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final static class AnyIterable implements Iterable {
        private final Iterable iterable;

        AnyIterable(Iterable iterable) {
            this.iterable = iterable;
        }

        @Override
        public Iterator iterator() {
            return new AnyIterator(iterable.iterator());
        }
    }

    private final static class AnyTupleIterable implements Iterable<AnyTuple> {
        private final Map map;

        AnyTupleIterable(Map map) {
            this.map = map;
        }

        @Override
        public Iterator<AnyTuple> iterator() {
            return new AnyTupleIterator(map.entrySet().iterator());
        }
    }

    private final static class AnyTupleIterator implements Iterator<AnyTuple> {

        private final Iterator iterator;

        AnyTupleIterator(Iterator iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public AnyTuple next() {
            Map.Entry<String, Object> me = (Map.Entry<String, Object>) iterator.next();
            String key = me.getKey();
            Object o = me.getValue();
            if (o instanceof Iterable) {
                return new AnyTuple(key, new AnyIterable((Iterable) o));
            }
            if (o instanceof Map) {
                return new AnyTuple(key, new MapBasedAnyObject((Map) o));
            }
            return new AnyTuple(key, o);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}