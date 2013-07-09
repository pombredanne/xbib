package org.xbib.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CompactHashMapTest extends Assert {

    private void assertTrue(String a, boolean b) {
        assertTrue(b, a);
    }

    private void assertFalse(String a, boolean b) {
        assertFalse(b, a);
    }

    @Test
    public void testEmpty() {
        Map map = new CompactHashMap();
        assertTrue("empty map doesn't know it's empty", map.isEmpty());
        assertTrue("empty map doesn't know it's empty", map.isEmpty());
        assertFalse("empty map claims to contain key", map.containsKey(""));
        assertFalse("empty map claims to contain value", map.containsValue(""));
        assertFalse("empty map claims to contain null", map.containsKey(null));
        assertFalse("empty map claims to contain null", map.containsValue(null));
        assertTrue("removing non-existent key returns value",
                map.remove("foo") == null);

        // testing key set
        Set keys = map.keySet();
        assertTrue("empty map keyset is non-empty", keys.isEmpty());
        assertTrue("empty map keyset is non-empty", keys.isEmpty());
        assertFalse("empty map keyset contains value", keys.contains("foo"));
        assertFalse("empty map keyset contains value", keys.contains(null));
        Iterator it = keys.iterator();
        assertFalse("empty map keyset iterator returns value", it.hasNext());

        // testing values collection
        Collection values = map.values();
        assertTrue("empty map value collection is non-empty",
                values.isEmpty());
        assertTrue("empty map value collection is non-empty", values.isEmpty());
        assertFalse("empty map value collection contains non-value",
                values.contains("bar"));
        assertFalse("empty map value collection contains non-value",
                values.contains(null));
        it = values.iterator();
        assertFalse("empty map value collection iterator returns value",
                it.hasNext());
    }

    @Test
    public void testAddOne() {
        Map map = new CompactHashMap();
        assertTrue("empty map claimed previous mapping for key",
                map.put("1", "one") == null);
        assertTrue("wrong size for map", map.size() == 1);
        assertFalse("map claims to be empty", map.isEmpty());
        assertTrue("map doesn't recognize key", map.containsKey("1"));
        assertTrue("map doesn't recognize value", map.containsValue("one"));
        assertFalse("map recognizes missing key", map.containsKey("2"));
        assertFalse("map recognizes missing value", map.containsValue("two"));
        assertFalse("map recognizes missing key", map.containsKey(null));
        assertFalse("map recognizes missing value", map.containsValue(null));

        assertTrue("map returns wrong value for key", map.get("1").equals("one"));

        // testing key set
        Set keys = map.keySet();
        assertFalse("non-empty map keyset is empty", keys.isEmpty());
        assertTrue("map keyset has wrong size", keys.size() == 1);
        assertFalse("map keyset contains wrong value", keys.contains("foo"));
        assertFalse("map keyset contains wrong value", keys.contains(null));
        assertTrue("map keyset lacks value", keys.contains("1"));

        Iterator it = keys.iterator();
        assertTrue("map keyset iterator has no values", it.hasNext());
        assertTrue("map keyset iterator produces wrong value",
                it.next().equals("1"));
        assertFalse("map keyset iterator has too many values", it.hasNext());

        // testing values collection
        Collection values = map.values();
        assertFalse("map value collection is empty",
                values.isEmpty());
        assertTrue("empty map value collection has wrong size",
                values.size() == 1);
        assertFalse("value collection contains non-value",
                values.contains("bar"));
        assertFalse("value collection contains non-value",
                values.contains(null));
        assertTrue("value collection cannot find value",
                values.contains("one"));
        it = values.iterator();
        assertTrue("value collection iterator is empty",
                it.hasNext());
        assertTrue("value collection iterator returns wrong value",
                it.next().equals("one"));
        assertFalse("value collection iterator has too many values",
                it.hasNext());

        // --- emptying the map
        assertTrue("removing mapping finds wrong value",
                map.remove("1").equals("one"));

    }

    @Test
    public void testAddOneToTen() {
        Map map = new CompactHashMap();

        Set allkeys = new HashSet();
        Set allvalues = new HashSet();

        for (int ix = 1; ix <= 10; ix++) {
            assertTrue("map erroneously claimed previous mapping for key",
                    map.put("" + ix, new Integer(ix)) == null);
            allkeys.add("" + ix);
            allvalues.add(new Integer(ix));
        }
        assertTrue("wrong size for map", map.size() == 10);
        assertFalse("map claims to be empty", map.isEmpty());

        for (int ix = 1; ix <= 10; ix++) {
            assertTrue("map doesn't recognize key " + ix, map.containsKey("" + ix));
            assertTrue("map doesn't recognize value",
                    map.containsValue(new Integer(ix)));
            assertTrue("map returns wrong value for key",
                    map.get("" + ix).equals(new Integer(ix)));
        }

        assertFalse("map recognizes missing key", map.containsKey("x"));
        assertFalse("map recognizes missing value", map.containsValue("two"));
        assertFalse("map recognizes missing key", map.containsKey(null));
        assertFalse("map recognizes missing value", map.containsValue(null));

        // testing key set
        Set keys = map.keySet();
        assertFalse("non-empty map keyset is empty", keys.isEmpty());
        assertTrue("map keyset has wrong size", keys.size() == 10);
        assertFalse("map keyset contains wrong value", keys.contains("foo"));
        assertFalse("map keyset contains wrong value", keys.contains(null));
        for (int ix = 1; ix <= 10; ix++) {
            assertTrue("map keyset lacks value", keys.contains("" + ix));
        }

        Iterator it = keys.iterator();
        while (it.hasNext()) {
            Object key = it.next();
            assertTrue("map keyset produces non-key value",
                    allkeys.contains(key));
            allkeys.remove(key);
        }
        assertTrue("map keyset doesn't contain all keys", allkeys.isEmpty());

        // testing values collection
        Collection values = map.values();
        assertFalse("map value collection is empty",
                values.isEmpty());
        assertTrue("empty map value collection has wrong size",
                values.size() == 10);
        assertFalse("value collection contains non-value",
                values.contains("bar"));
        assertFalse("value collection contains non-value",
                values.contains(null));
        assertTrue("value collection cannot find value",
                values.contains(new Integer(5)));

        it = values.iterator();
        while (it.hasNext()) {
            Object value = it.next();
            assertTrue("map value collection iterator produces nonvalue",
                    allvalues.contains(value));
            allvalues.remove(value);
        }
        assertTrue("map value collection doesn't contain all values",
                allvalues.isEmpty());

        // --- emptying the map
        for (int ix = 1; ix <= 10; ix++) {
            assertTrue("removing mapping finds wrong value",
                    map.remove("" + ix).equals(new Integer(ix)));
        }
    }

    @Test
    public void testClear() {
        Map map = new CompactHashMap();
        for (int ix = 1; ix <= 10; ix++) {
            assertTrue("map erroneously claimed previous mapping for key",
                    map.put("" + ix, new Integer(ix)) == null);
        }
        map.clear();
    }

    @Test
    public void testOverwrite() {
        Map map = new CompactHashMap();
        for (int ix = 1; ix <= 10; ix++) {
            assertTrue("map erroneously claimed previous mapping for key",
                    map.put("" + ix, new Integer(ix)) == null);
        }

        map.put("" + 5, "five");

        for (int ix = 1; ix <= 10; ix++) {
            if (ix == 5) {
                assertTrue("map doesn't recognize key " + ix, map.containsKey("" + ix));
                assertTrue("map doesn't recognize value",
                        map.containsValue("five"));
                assertTrue("map returns wrong value for key",
                        map.get("" + ix).equals("five"));
            } else {
                assertTrue("map doesn't recognize key " + ix, map.containsKey("" + ix));
                assertTrue("map doesn't recognize value",
                        map.containsValue(new Integer(ix)));
                assertTrue("map returns wrong value for key",
                        map.get("" + ix).equals(new Integer(ix)));
            }
        }
    }

    @Test
    public void testRemoveAndReadd() {
        Map map = new CompactHashMap();
        for (int ix = 1; ix <= 10; ix++) {
            assertTrue("map erroneously claimed previous mapping for key",
                    map.put("" + ix, new Integer(ix)) == null);
        }

        map.remove("" + 5);
        map.put("" + 5, "five");

        for (int ix = 1; ix <= 10; ix++) {
            if (ix == 5) {
                assertTrue("map doesn't recognize key " + ix, map.containsKey("" + ix));
                assertTrue("map doesn't recognize value",
                        map.containsValue("five"));
                assertTrue("map returns wrong value for key",
                        map.get("" + ix).equals("five"));
            } else {
                assertTrue("map doesn't recognize key " + ix, map.containsKey("" + ix));
                assertTrue("map doesn't recognize value",
                        map.containsValue(new Integer(ix)));
                assertTrue("map returns wrong value for key",
                        map.get("" + ix).equals(new Integer(ix)));
            }
        }
    }
}
