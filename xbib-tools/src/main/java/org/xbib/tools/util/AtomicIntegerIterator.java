package org.xbib.tools.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread-safe integer iterator
 *
 */
public class AtomicIntegerIterator
        implements Iterator<Integer>, Iterable<Integer> {

    /**
     * thread-safe counter
     */
    private AtomicInteger count;
    private final int end;

    /**
     * Creates a new IntegerSequence object.
     *
     * @param start
     */
    public AtomicIntegerIterator(int start) {
        this(start, Integer.MAX_VALUE);
    }

    /**
     * Creates a new IntegerSequence object.
     *
     * @param start
     * @param end
     */
    public AtomicIntegerIterator(int start, int end) {
        this.count = new AtomicInteger(start);
        this.end = end;
    }

    /**
     * Check if iterator can produce next value
     *
     * @return true if max value reached
     */
    @Override
    public boolean hasNext() {
        return count.intValue() < end;
    }

    /**
     * Return integer of next value
     *
     * @return the integer
     *
     * @throws NoSuchElementException 
     */
    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return count.getAndIncrement();
    }

    /**
     * Remove operation is not supported
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public Iterator<Integer> iterator() {
        return this;
    }
}
