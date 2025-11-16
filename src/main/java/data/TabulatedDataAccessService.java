package data;

import functions.Insertable;
import functions.Point;
import functions.Removable;
import functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TabulatedDataAccessService {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedDataAccessService.class);

    private final TabulatedFunction function;

    public TabulatedDataAccessService(TabulatedFunction function) {
        this.function = Objects.requireNonNull(function, "Табулированная функция не может быть null");
        logger.info("Инициализирован сервис доступа к данным для функции с {} точками", function.getCount());
    }

    public List<Point> find(TabulatedDataQuery query) {
        Objects.requireNonNull(query, "Условие запроса не может быть null");
        List<Point> results = new ArrayList<>();
        logger.debug("Выполняем поиск по условию {}", query);
        for (Point point : function) {
            if (query.matches(point)) {
                results.add(point);
            }
        }
        logger.info("Поиск завершён, найдено {} точек", results.size());
        return results;
    }

    public Optional<Point> findFirst(TabulatedDataQuery query) {
        Objects.requireNonNull(query, "Условие запроса не может быть null");
        logger.debug("Ищем первую точку по условию {}", query);
        for (Point point : function) {
            if (query.matches(point)) {
                logger.info("Найдена подходящая точка ({}, {})", point.x, point.y);
                return Optional.of(point);
            }
        }
        logger.info("Подходящих точек не найдено");
        return Optional.empty();
    }

    public Point addPoint(double x, double y) {
        logger.debug("Добавляем точку ({}, {})", x, y);
        ensureInsertable();
        ((Insertable) function).insert(x, y);
        logger.info("Точка ({}, {}) успешно добавлена", x, y);
        return new Point(x, y);
    }

    public boolean updateY(double x, double newY) {
        logger.debug("Обновляем точку с x={} до нового y={}", x, newY);
        int index = function.indexOfX(x);
        if (index < 0) {
            logger.info("Точка с x={} не найдена для обновления", x);
            return false;
        }
        function.setY(index, newY);
        logger.info("Точка с x={} успешно обновлена на y={}", x, newY);
        return true;
    }

    public boolean deleteByX(double x) {
        logger.debug("Удаляем точку с x={}", x);
        int index = function.indexOfX(x);
        if (index < 0) {
            logger.info("Точка с x={} не найдена для удаления", x);
            return false;
        }
        ensureRemovable();
        ((Removable) function).remove(index);
        logger.info("Точка с x={} успешно удалена", x);
        return true;
    }

    private void ensureInsertable() {
        if (!(function instanceof Insertable)) {
            logger.error("Функция не поддерживает добавление новых точек");
            throw new IllegalStateException("Функция не поддерживает добавление точек");
        }
    }

    private void ensureRemovable() {
        if (!(function instanceof Removable)) {
            logger.error("Функция не поддерживает удаление точек");
            throw new IllegalStateException("Функция не поддерживает удаление точек");
        }
    }
}