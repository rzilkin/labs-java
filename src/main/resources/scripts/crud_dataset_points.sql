SELECT point_index, x_value, y_value
FROM dataset_points
WHERE dataset_id = 5
ORDER BY point_index;
SELECT
    dp.point_index,
    dp.x_value,
    dp.y_value,
    mf.name AS function_name,
    u.username AS owner
FROM dataset_points dp
JOIN tabulated_datasets td ON dp.dataset_id = td.id
JOIN math_functions mf ON td.function_id = mf.id
JOIN users u ON mf.owner_id = u.id
WHERE dp.dataset_id = 5
ORDER BY dp.point_index;
SELECT point_index, x_value, y_value
FROM dataset_points
WHERE dataset_id = 5
  AND x_value BETWEEN 0.5 AND 1.5
ORDER BY x_value;

INSERT INTO dataset_points (dataset_id, point_index, x_value, y_value)
VALUES (5, 0, 0.0, 0.0);
INSERT INTO dataset_points (dataset_id, point_index, x_value, y_value)
VALUES
    (5, 0, 0.0,  0.0),
    (5, 1, 0.5,  0.4794),
    (5, 2, 1.0,  0.8415),
    (5, 3, 1.5,  0.9975),
    (5, 4, 2.0,  0.9093);
INSERT INTO dataset_points (dataset_id, point_index, x_value, y_value)
VALUES (5, 2, 1.0, 0.8415)
ON CONFLICT (dataset_id, point_index) DO NOTHING;

UPDATE dataset_points
SET y_value = 0.8414709848
WHERE dataset_id = 5 AND point_index = 2;
UPDATE dataset_points
SET x_value = 1.001, y_value = 0.842
WHERE dataset_id = 5 AND point_index = 2;
UPDATE dataset_points
SET x_value = x_value + 0.1
WHERE dataset_id = 5;

DELETE FROM dataset_points
WHERE dataset_id = 5 AND point_index = 2;
DELETE FROM dataset_points
WHERE dataset_id = 5;
DELETE FROM dataset_points
WHERE dataset_id = 5 AND (x_value < 0 OR x_value > 2);