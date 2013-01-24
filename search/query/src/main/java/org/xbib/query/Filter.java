package org.xbib.query;

import java.util.Collection;
import java.util.Iterator;

public class Filter<S, T> {

    public interface Predicate<S, T> {

        T apply(S s);
    }    
    
    public static <S,T> void filter(Iterator<S> source, Collection<T> target, Predicate<S, T> p) {
        while (source.hasNext()) {
            T t = p.apply(source.next());
            if (t != null) {
                target.add(t);
            }
        }
    }
    
    
    public static <S,T> void filter(Iterable<S> source, Collection<T> target, Predicate<S, T> p) {
        for (S s : source) {
            T t = p.apply(s);
            if (t != null) {
                target.add(t);
            }
        }
    }
    
    
    public static <S,T> void filter(S[] source, Collection<T> target, Predicate<S, T> p) {
        for (S s : source) {
            T t = p.apply(s);
            if (t != null) {
                target.add(t);
            }
        }        
    }
    
}
