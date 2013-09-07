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
package org.xbib.util;

import java.util.Map;
import java.util.TreeMap;

/**
 * <p>A map whose keys are strings; when a key/value pair is added to the map,
 * the longest unique abbreviations of that key are added as well, and associated with
 * the value.  Thus:</p>
 * <p/>
 * <pre>
 *   <code>
 *   abbreviations.put( "good", "bye" );
 *   </code>
 * </pre>
 * <p/>
 * <p>would make it such that you could retrieve the value {@code "bye"} from the map
 * using the keys {@code "good"}, {@code "goo"}, {@code "go"}, and {@code "g"}.
 * A subsequent invocation of:</p>
 * <pre>
 *   <code>
 *   abbreviations.put( "go", "fish" );
 *   </code>
 * </pre>
 * <p/>
 * <p>would make it such that you could retrieve the value {@code "bye"} using the keys
 * {@code "good"} and {@code "goo"}, and the value {@code "fish"} using the key
 * {@code "go"}.  The key {@code "g"} would yield {@code null}, since it would no longer
 * be a unique abbreviation.</p>
 * <p/>
 * <p>The data structure is much like a "trie".</p>
 *
 * @param <V> a constraint on the types of the values in the map
 * @see <a href="http://www.perldoc.com/perl5.8.0/lib/Text/Abbrev.html">Perl's Text::Abbrev module</a>
 */
public class AbbreviationMap<V> {
    private String key;
    private V value;
    private final Map<Character, AbbreviationMap<V>> children = new TreeMap<Character, AbbreviationMap<V>>();
    private int keysBeyond;

    /**
     * <p>Tells whether the given key is in the map, or whether the given key is a unique
     * abbreviation of a key that is in the map.</p>
     *
     * @param aKey key to look up
     * @return {@code true} if {@code key} is present in the map
     * @throws NullPointerException if {@code key} is {@code null}
     */
    public boolean contains(String aKey) {
        return get(aKey) != null;
    }

    /**
     * <p>Answers the value associated with the given key.  The key can be a unique
     * abbreviation of a key that is in the map. </p>
     *
     * @param aKey key to look up
     * @return the value associated with {@code aKey}; or {@code null} if there is no
     *         such value or {@code aKey} is not a unique abbreviation of a key in the map
     * @throws NullPointerException if {@code aKey} is {@code null}
     */
    public V get(String aKey) {
        char[] chars = charsOf(aKey);

        AbbreviationMap<V> child = this;
        for (char each : chars) {
            child = child.children.get(each);
            if (child == null) {
                return null;
            }
        }

        return child.value;
    }

    /**
     * <p>Associates a given value with a given key.  If there was a previous
     * association, the old value is replaced with the new one.</p>
     *
     * @param aKey     key to create in the map
     * @param newValue value to associate with the key
     * @throws NullPointerException     if {@code aKey} or {@code newValue} is {@code null}
     * @throws IllegalArgumentException if {@code aKey} is a zero-length string
     */
    public void put(String aKey, V newValue) {
        if (newValue == null) {
            throw new NullPointerException();
        }
        if (aKey.length() == 0) {
            throw new IllegalArgumentException();
        }

        char[] chars = charsOf(aKey);
        add(chars, newValue, 0, chars.length);
    }

    /**
     * <p>Associates a given value with a given set of keys.  If there was a previous
     * association, the old value is replaced with the new one.</p>
     *
     * @param keys     keys to create in the map
     * @param newValue value to associate with the key
     * @throws NullPointerException     if {@code keys} or {@code newValue} is {@code null}
     * @throws IllegalArgumentException if any of {@code keys} is a zero-length string
     */
    public void putAll(Iterable<String> keys, V newValue) {
        for (String each : keys) {
            put(each, newValue);
        }
    }

    private boolean add(char[] chars, V newValue, int offset, int length) {
        if (offset == length) {
            value = newValue;
            boolean wasAlreadyAKey = key != null;
            key = new String(chars);
            return !wasAlreadyAKey;
        }

        char nextChar = chars[offset];
        AbbreviationMap<V> child = children.get(nextChar);
        if (child == null) {
            child = new AbbreviationMap<V>();
            children.put(nextChar, child);
        }

        boolean newKeyAdded = child.add(chars, newValue, offset + 1, length);

        if (newKeyAdded) {
            ++keysBeyond;
        }

        if (key == null) {
            value = keysBeyond > 1 ? null : newValue;
        }

        return newKeyAdded;
    }

    /**
     * <p>If the map contains the given key, dissociates the key from its value.</p>
     *
     * @param aKey key to remove
     * @throws NullPointerException     if {@code aKey} is {@code null}
     * @throws IllegalArgumentException if {@code aKey} is a zero-length string
     */
    public void remove(String aKey) {
        if (aKey.length() == 0) {
            throw new IllegalArgumentException();
        }

        char[] keyChars = charsOf(aKey);
        remove(keyChars, 0, keyChars.length);
    }

    private boolean remove(char[] aKey, int offset, int length) {
        if (offset == length) {
            return removeAtEndOfKey();
        }

        char nextChar = aKey[offset];
        AbbreviationMap<V> child = children.get(nextChar);
        if (child == null || !child.remove(aKey, offset + 1, length)) {
            return false;
        }

        --keysBeyond;
        if (child.keysBeyond == 0) {
            children.remove(nextChar);
        }
        if (keysBeyond == 1 && key == null) {
            setValueToThatOfOnlyChild();
        }

        return true;
    }

    private void setValueToThatOfOnlyChild() {
        Map.Entry<Character, AbbreviationMap<V>> entry = children.entrySet().iterator().next();
        AbbreviationMap<V> onlyChild = entry.getValue();
        value = onlyChild.value;
    }

    private boolean removeAtEndOfKey() {
        if (key == null) {
            return false;
        }

        key = null;
        if (keysBeyond == 1) {
            setValueToThatOfOnlyChild();
        } else {
            value = null;
        }

        return true;
    }

    /**
     * Gives a Java map representation of this abbreviation map.
     *
     * @return a Java map corresponding to this abbreviation map
     */
    public Map<String, V> toJavaUtilMap() {
        Map<String, V> mappings = new TreeMap<String, V>();
        addToMappings(mappings);
        return mappings;
    }

    private void addToMappings(Map<String, V> mappings) {
        if (key != null) {
            mappings.put(key, value);
        }

        for (AbbreviationMap<V> each : children.values()) {
            each.addToMappings(mappings);
        }
    }

    private static char[] charsOf(String aKey) {
        char[] chars = new char[aKey.length()];
        aKey.getChars(0, aKey.length(), chars, 0);
        return chars;
    }
}
