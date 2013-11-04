package org.xbib.re;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class Generics {

    public static <T> List<T> newArrayList() {
        return new ArrayList<>();
    }

    public static <T> List<T> newArrayList(Collection<? extends T> c) {
        return new ArrayList<>(c);
    }

    public static <T> List<T> newLinkedList() {
        return new LinkedList<>();
    }

    public static <T> Set<T> newHashSet() {
        return new HashSet<>();
    }

    public static <T, S> Map<T, S> newHashMap() {
        return new HashMap<>();
    }

    public static <T, S> Map<T, S> newTreeMap() {
        return new TreeMap<>();
    }

    public static <T, S> Map<T, S> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    public static <T, S> Map<S, List<T>> bucket(Collection<? extends T> c, Function<T, S> f) {
        Map<S, List<T>> buckets = Generics.newHashMap();
        for (T value : c) {
            S key = f.eval(value);
            List<T> bucket = buckets.get(key);
            if (bucket == null) {
                buckets.put(key, bucket = Generics.newArrayList());
            }
            bucket.add(value);
        }
        return buckets;
    }

    public static <T> Collection filter(Collection<? extends T> c, Function<T, Boolean> p) {
        Iterator<? extends T> it = c.iterator();
        while (it.hasNext()) {
            if (!p.eval(it.next())) {
                it.remove();
            }
        }
        return c;
    }
}
