package de.nescio.androidbot_test.utils;

/**
 * Created by Wohnzimmer on 16.11.2014.
 */
public class Point {
    public int x, y;

    public Point(int _x, int _y) {
        x = _x;
        y = _y;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o == null || o.getClass() != getClass()) {
            result = false;
        } else {
            Point object = (Point) o;
            if (object.x == x && object.y == y) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + x;
        hash = 7 * hash + y;
        return hash;
    }
}
