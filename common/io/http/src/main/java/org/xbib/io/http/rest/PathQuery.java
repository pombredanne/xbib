package org.xbib.io.http.rest;

/**
 * Simple abstraction for queries into complex datastructures.
 * Not really needed, but I like playing around with generics. :)
 */

public abstract class PathQuery<T, S> {
    abstract S eval(T resource) throws Exception;
}
