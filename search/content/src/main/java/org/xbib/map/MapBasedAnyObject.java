/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapBasedAnyObject implements AnyObject {

    private final Map map;

    public MapBasedAnyObject(Map map) {
        this.map = new LinkedHashMap(map);
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
        return (T) (o instanceof Map && key.length > 1 ?
            get((Map) o, Arrays.copyOfRange(key, 1, key.length)) : o);
    }

    <T> T getAll(String key) {
        return this.<T>getAll(map, key.split("\\."));
    }

    private <T> T getAll(Map inner, String[] key) {
        if (inner == null) {
            return null;
        }
        Object o = inner.get(key[0]);
        return (T) (o instanceof Map && key.length > 1 ?
                getAll((Map) o, Arrays.copyOfRange(key, 1, key.length)) : o);
    }

    @Override
    public <T> T getAnyObject(String key) {
        return get(key);
    }

    @Override
    public <T> Iterable<T> getIterable(final String key) {
        final Object o = getAll(key);
        if (o == null) {
            return null;
        }
        if (o instanceof Iterable) {
            return  (Iterable<T>)o;
        }
        return new ArrayList<T>() {{ add((T)o); }};
    }

    @Override
    public Iterable<AnyTuple> getTuples() {
        return new AnyTupleIterable(map);
    }

    @Override
    public Long getLong(String key) {
        return get(key);
    }

    public String getString(String key) {
        return get(key);
    }

    @Override
    public Integer getInteger(String key) {
        Object o = get(key);
        return o == null ? null : o instanceof Integer ? (Integer) o : Integer.parseInt(o.toString());
    }

    public Double getDouble(String key) {
        Object o = get(key);
        return o == null ? null : o instanceof Double ? (Double) o : Double.parseDouble(o.toString());
    }

    public Float getFloat(String key) {
        Object o = get(key);
        return o == null ? null : o instanceof Float ? (Float) o : Float.parseFloat(o.toString());
    }

    public Boolean getBoolean(String key) {
        return get(key);
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