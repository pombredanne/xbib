/*
 * Copyright (C) 2010 Zhenya Leonov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xbib.collection;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.SortedSet;

import com.google.common.collect.Ordering;

/**
 * An unbounded double-ended priority queue (also known as a {@link java.util.Deque})
 * based on a modified <a
 * href="http://en.wikipedia.org/wiki/Red-black_tree">Red-Black Tree</a>. The
 * elements of this deque are sorted according to their <i>natural ordering</i>,
 * or by an explicit {@link java.util.Comparator} provided at creation. Inserting
 * {@code null} elements will fail cleanly and safely leaving this deque
 * unmodified. Querying for {@code null} elements is allowed. Attempting to
 * insert non-comparable elements will result in a {@code ClassCastException} .
 * The {@code addFirst(E)}, {@code addLast(E)}, {@code offerFirst(E)},
 * {@code offerLast(E)}, and {@code push(E)} operations are not supported.
 * <p>
 * This deque is ordered from <i>least</i> to <i>greatest</i> with respect to
 * the specified ordering. Elements with equal priority are ordered according to
 * their insertion order.
 * <p>
 * The {@link #iterator() iterator()} and {@link #descendingIterator()} methods
 * return <i>fail-fast</i> iterators which are guaranteed to traverse the
 * elements of the deque in priority and reverse priority order, respectively.
 * Attempts to modify the elements in this deque at any time after an iterator
 * is created, in any way except through the iterator's own remove method, will
 * result in a {@code ConcurrentModificationException}.
 * <p>
 * This deque is not <i>thread-safe</i>. If multiple threads modify this deque
 * concurrently it must be synchronized externally.
 * <p>
 * <b>Implementation Note:</b> This implementation uses a comparator (whether or
 * not one is explicitly provided) to maintain priority order, and
 * {@code equals} when testing for element equality. The ordering imposed by the
 * comparator is not required to be <i>consistent with equals</i>. Given a
 * comparator {@code c}, for any two elements {@code e1} and {@code e2} such
 * that {@code c.compare(e1, e2) == 0} it is not necessarily true that
 * {@code e1.equals(e2) == true}.
 * <p>
 * The underlying Red-Black Tree provides the following worst case running time
 * (where <i>n</i> is the size of this list and <i>m</i> is the size of the
 * specified collection):
 * <p>
 * <table border="1" cellpadding="3" cellspacing="1" style="width:400px;">
 *   <tr>
 *     <th style="text-align:center;">Method</th>
 *     <th style="text-align:center;">Running Time</th>
 *   </tr>
 *   <tr>
 *     <td>
 *       {@link #addAll(java.util.Collection) addAll(Collection)}<br>
 *       {@link #containsAll(java.util.Collection) containsAll(Collection)}</br>
 *       {@link #retainAll(java.util.Collection) retainAll(Collection)}</br>
 *       {@link #removeAll(java.util.Collection) removeAll(Collection)}
 *     </td>
 *     <td style="text-align:center;">
 *       <i>O(m log n)</i>
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>
 *       {@link #add(Object) add(E)}</br>
 *       {@link #contains(Object) contains(Object)}</br>
 *       {@link #offer(Object) offer(E)}</br>
 *       {@link #remove(Object) remove(Object)}
 *     </td>
 *     <td style="text-align:center;">
 *       <i>O(log n)</i></td>
 *   </tr>
 *   <tr>
 *     <td>
 *       {@link #element() element()}</br>
 *       {@link #isEmpty() isEmpty()}</br>
 *       {@link #peek() peek()}</br>
 *       {@link #poll() poll()}</br>
 *       {@link #remove() remove()}</br>
 *       {@link #size() size()}<br>
 *       {@link #getFirst() getFirst()}</br>
 *       {@link #getLast() getLast()}</br>
 *       {@link #peekFirst() peekFirst()}</br>
 *       {@link #peekLast() peekLast()}</br>
 *       {@link #pollFirst() pollFirst()}</br>
 *       {@link #pollLast() pollLast()}</br>
 *       {@link #pop() pop()}</br>
 *       {@link #removeFirst() removeFirst()}</br>
 *       {@link #removeLast() removeLast()}</br>
 *       {@link #clear()}
 *     </td>
 *     <td style="text-align:center;"><i>O(1)</i></td>
 *   </tr>
 * </table>
 * <p>
 * Refer to {@link TreeQueue} for a comparison with {@link java.util.PriorityQueue
 * java.util.PriorityQueue}.
 * <p>
 * Note: This deque uses the same ordering rules as
 * {@code java.util.PriorityQueue}. In comparison it offers element operations
 * at both ends, ordered traversals via its iterators, and faster overall
 * running time.
 * 
 * @param <E> the type of elements held in this deque
 */
public class TreeDeque<E> extends TreeQueue<E> implements Deque<E> {

	transient Node max = nil;

    public TreeDeque() {
        super((Comparator<? super E>)Ordering.natural());
    }

	public TreeDeque(final Comparator<? super E> comparator) {
		super(comparator);
	}

	public TreeDeque(final Comparator<? super E> comparator,
			Iterable<? extends E> elements) {
		super(comparator, elements);
	}

	/**
	 * Creates a new {@code TreeDeque} that orders its elements according to
	 * their <i>natural ordering</i>.
	 * 
	 * @return a new {@code TreeDeque} that orders its elements according to
	 *         their <i>natural ordering</i>
	 */
	public static <E extends Comparable<? super E>> TreeDeque<E> create() {
		return new TreeDeque<E>(Ordering.natural());
	}

	/**
	 * Creates a new {@code TreeDeque} that orders its elements according to the
	 * specified comparator.
	 * 
	 * @param comparator
	 *            the comparator that will be used to order this deque
	 * @return a new {@code TreeDeque} that orders its elements according to
	 *         {@code comparator}
	 */
	public static <E> TreeDeque<E> create(final Comparator<? super E> comparator) {
		checkNotNull(comparator);
		return new TreeDeque<E>(comparator);
	}

	/**
	 * Creates a new {@code TreeDeque} containing the elements of the specified
	 * {@code Iterable}. If the specified iterable is an instance of
	 * {@link java.util.SortedSet}, {@link java.util.PriorityQueue} or {@link SortedCollection} this
	 * deque will be ordered according to the same ordering. Otherwise, this
	 * deque will be ordered according to the <i>natural ordering</i> of its
	 * elements.
	 * 
	 * @param elements
	 *            the iterable whose elements are to be placed into the deque
	 * @return a new {@code TreeDeque} containing the elements of the specified
	 *         iterable
	 * @throws ClassCastException
	 *             if elements of the specified iterable cannot be compared to
	 *             one another according to the this deque's ordering
	 * @throws NullPointerException
	 *             if any of the elements of the specified iterable or the
	 *             iterable itself is {@code null}
	 */
	public static <E> TreeDeque<E> create(final Iterable<? extends E> elements) {
		checkNotNull(elements);
		final Comparator<? super E> comparator;
		if (elements instanceof SortedSet<?>)
			comparator = ((SortedSet) elements).comparator();
		else if (elements instanceof PriorityQueue<?>)
			comparator = ((PriorityQueue) elements).comparator();
		else if (elements instanceof SortedCollection<?>)
			comparator = ((SortedCollection) elements).comparator();
		else
			comparator = (Comparator<? super E>) Ordering.natural();
		return new TreeDeque<E>(comparator, elements);
	}
	
//	/**
//	 * Creates a {@code TreeDeque} containing the specified initial elements
//	 * sorted according to their <i>natural ordering</i>.
//	 * 
//	 * @param elements
//	 *            the initial elements to be placed in this deque
//	 * @return a {@code TreeDeque} containing the specified initial elements
//	 *         sorted according to their <i>natural ordering</i>
//	 */
//	public static <E extends Comparable<? super E>> TreeDeque<E> create(
//			final E... elements) {
//		checkNotNull(elements);
//		TreeDeque<E> d = TreeDeque.create();
//		Collections.addAll(d, elements);
//		return d;
//	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void addFirst(E e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void addLast(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<E> descendingIterator() {
		return new Iterator<E>() {
			private Node next = max;
			private Node last = nil;
			private int expectedModCount = modCount;

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public void remove() {
				checkForConcurrentModification();
				if (last == null)
					throw new IllegalStateException();
				if (last.left != nil && last.right != nil)
					next = last;
				delete(last);
				expectedModCount = modCount;
				last = nil;
			}

			@Override
			public E next() {
				checkForConcurrentModification();
				Node node = next;
				if (node == nil)
					throw new NoSuchElementException();
				next = predecessor(node);
				last = node;
				return node.element;
			}

			private void checkForConcurrentModification() {
				if (modCount != expectedModCount)
					throw new ConcurrentModificationException();
			}
		};
	}

	@Override
	public E getFirst() {
		return element();
	}

	@Override
	public E getLast() {
		E e = peekLast();
		if (e != null)
			return e;
		else
			throw new NoSuchElementException();
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public boolean offerFirst(E e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public boolean offerLast(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E peekFirst() {
		return peek();
	}

	@Override
	public E peekLast() {
		if (isEmpty())
			return null;
		return max.element;
	}

	@Override
	public E pollFirst() {
		return poll();
	}

	@Override
	public E pollLast() {
		if (isEmpty())
			return null;
		E e = max.element;
		delete(max);
		return e;
	}

	@Override
	public E pop() {
		return remove();
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void push(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E removeFirst() {
		return remove();
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	public boolean removeFirstOccurrence(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E removeLast() {
		E e = pollLast();
		if (e != null)
			return e;
		else
			throw new NoSuchElementException();
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	public boolean removeLastOccurrence(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		super.clear();
		max = nil;
	}

	/**
	 * Returns a shallow copy of this {@code TreeDeque}. The elements themselves
	 * are not cloned.
	 * 
	 * @return a shallow copy of this deque
	 */
	@Override
	public TreeDeque<E> clone() {
		TreeDeque<E> clone = (TreeDeque<E>) super.clone();
		clone.nil = new Node();
		clone.modCount = 0;
		clone.root = clone.nil;
		clone.min = clone.nil;
		clone.max = clone.nil;
		clone.size = 0;
		clone.addAll(this);
		return clone;
	}

	private void readObject(java.io.ObjectInputStream ois)
			throws java.io.IOException, ClassNotFoundException {
		ois.defaultReadObject();
		nil = new Node();
		root = nil;
		max = nil;
		min = nil;
		int size = ois.readInt();
		for (int i = 0; i < size; i++)
			add((E) ois.readObject());
	}

	/*
	 * Red-Black Tree
	 */

	@Override
	void insert(final Node z) {
		super.insert(z);
		if (max == nil || comparator.compare(z.element, max.element) >= 0)
			max = z;
	}

	@Override
	void delete(final Node z) {
		if (max == z)
			max = predecessor(z);
		super.delete(z);
	}

	private Node predecessor(Node x) {
		Node y;
		if (x == nil)
			return nil;
		if (x.left != nil) {
			y = x.left;
			while (y.right != nil)
				y = y.right;
			return y;
		}
		y = x.parent;
		while (y != nil && x == y.left) {
			x = y;
			y = y.left;
		}
		return y;
	}

}