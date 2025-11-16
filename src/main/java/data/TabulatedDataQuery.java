package data;

import functions.Point;

@FunctionalInterface
public interface TabulatedDataQuery {
    boolean matches(Point point);
}