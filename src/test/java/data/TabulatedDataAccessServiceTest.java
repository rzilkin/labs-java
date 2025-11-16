package data;

import functions.ArrayTabulatedFunction;
import functions.Point;
import functions.TabulatedFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedDataAccessServiceTest {

    private TabulatedDataAccessService service;
    private TabulatedFunction function;

    @BeforeEach
    void setUp() {
        double[] xValues = generateSortedArray(10, 0.0, 20.0);
        double[] yValues = generateRandomArray(xValues.length, -5.0, 5.0);
        function = new ArrayTabulatedFunction(xValues, yValues);
        service = new TabulatedDataAccessService(function);
    }

    @Test
    void findShouldReturnPointsByPredicate() {
        TabulatedDataQuery query = point -> point.x > 5 && point.x < 15 && point.y >= 0;
        List<Point> expected = collectMatching(query);
        List<Point> found = service.find(query);

        assertEquals(expected.size(), found.size());
        assertTrue(found.stream().allMatch(p -> p.x > 5 && p.x < 15 && p.y >= 0));
        for (int i = 0; i < expected.size(); i++) {
            Point e = expected.get(i);
            Point a = found.get(i);
            assertEquals(e.x, a.x, 1e-12, "x differs at index " + i);
            assertEquals(e.y, a.y, 1e-12, "y differs at index " + i);
        }
    }

    @Test
    void addPointShouldInsertAndBeQueryable() {
        Point added = service.addPoint(21.5, 42.0);

        assertNotNull(added);
        assertEquals(21.5, added.x);
        Optional<Point> found = service.findFirst(p -> p.x == 21.5);
        assertTrue(found.isPresent());
        assertEquals(42.0, found.get().y);
    }

    @Test
    void findFirstShouldReturnEmptyWhenNoMatch() {
        Optional<Point> found = service.findFirst(point -> point.x < -1000);
        assertTrue(found.isEmpty());
    }

    @Test
    void updateYShouldModifyExistingValue() {
        double targetX = function.getX(0);
        double oldY = function.getY(0);
        assertNotEquals(99.9, oldY);

        boolean updated = service.updateY(targetX, 99.9);
        assertTrue(updated);
        assertEquals(99.9, function.getY(0));
    }

    @Test
    void updateYShouldReturnFalseWhenMissing() {
        boolean updated = service.updateY(-100.0, 1.0);
        assertFalse(updated);
    }

    @Test
    void deleteByXShouldRemovePoint() {
        double removableX = function.getX(function.getCount() / 2);
        boolean removed = service.deleteByX(removableX);

        assertTrue(removed);
        assertEquals(-1, function.indexOfX(removableX));
    }

    @Test
    void deleteByXShouldReturnFalseWhenMissing() {
        boolean removed = service.deleteByX(999.0);
        assertFalse(removed);
    }

    private double[] generateSortedArray(int size, double min, double max) {
        Random random = new SecureRandom();
        return DoubleStream.generate(() -> min + (max - min) * random.nextDouble())
                .limit(size)
                .boxed()
                .sorted(Comparator.naturalOrder())
                .mapToDouble(Double::doubleValue)
                .toArray();
    }

    private double[] generateRandomArray(int size, double min, double max) {
        Random random = new SecureRandom();
        return DoubleStream.generate(() -> min + (max - min) * random.nextDouble())
                .limit(size)
                .toArray();
    }

    private List<Point> collectMatching(TabulatedDataQuery query) {
        List<Point> result = new java.util.ArrayList<>();
        for (Point point : function) {
            if (query.matches(point)) {
                result.add(point);
            }
        }
        return result;
    }
}