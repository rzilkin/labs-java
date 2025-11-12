SELECT id, engine, operation, records_processed, elapsed_ms
FROM performance_metrics
ORDER BY id DESC;
SELECT * FROM performance_metrics WHERE engine = 'FRAMEWORK_ORM';
SELECT
    operation,
    engine,
    records_processed,
    elapsed_ms,
    (records_processed::numeric / elapsed_ms * 1000) AS rec_per_sec
FROM performance_metrics
WHERE elapsed_ms > 0
ORDER BY elapsed_ms DESC
LIMIT 5;

INSERT INTO performance_metrics (engine, operation, records_processed, elapsed_ms)
VALUES ('FRAMEWORK_ORM', 'insert_math_functions', 1000, 85);
INSERT INTO performance_metrics (engine, operation, records_processed, elapsed_ms)
VALUES ('MANUAL_JDBC', 'insert_math_functions', 1000, 42);
INSERT INTO performance_metrics (engine, operation, records_processed, elapsed_ms)
VALUES
    ('FRAMEWORK_ORM', 'select_with_join', 5000, 120),
    ('MANUAL_JDBC', 'select_with_join', 5000, 68),
    ('FRAMEWORK_ORM', 'update_batch', 2000, 95),
    ('MANUAL_JDBC', 'update_batch', 2000, 41);

UPDATE performance_metrics
SET elapsed_ms = elapsed_ms / 1000
WHERE id = 42 AND elapsed_ms > 10000;
UPDATE performance_metrics
SET operation = 'insert_tabulated_datasets'
WHERE operation = 'insret_tabulated_datasets';

DELETE FROM performance_metrics WHERE id = 7;
DELETE FROM performance_metrics WHERE engine = 'OLD_ORM';
DELETE FROM performance_metrics;