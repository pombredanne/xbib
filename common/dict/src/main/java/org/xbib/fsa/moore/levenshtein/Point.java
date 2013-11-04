
package org.xbib.fsa.moore.levenshtein;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class Point implements Comparable<Point> {

    private Type type;

    private int x;

    private int y;

    public Point(Type type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public static Set<Point> emptySet() {
        return new HashSet();
    }

    public static Set<Point> newSet(Point... points) {
        return new HashSet(Arrays.asList(points));
    }

    public Type getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean equals(Point p) {
        return compareTo(p) == 0;
    }

    @Override
    public int compareTo(Point p) {
        if (x < p.x) {
            return -1;
        } else if ((x == p.x) && (y < p.y)) {
            return -1;
        } else if ((x == p.x) && (y == p.y) && type == p.type) {
            return 0;
        } else {
            return 1;
        }

    }
}
