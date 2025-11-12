SELECT id, owner_id, name, function_type FROM math_functions;
SELECT id, name, function_type, definition_body
FROM math_functions
WHERE owner_id = 1;
SELECT mf.id, mf.name, mf.function_type, u.username
FROM math_functions mf
JOIN users u ON mf.owner_id = u.id
WHERE u.username = 'alice';
SELECT * FROM math_functions WHERE id = 42;
SELECT * FROM math_functions
WHERE owner_id = 1 AND name = 'sin(x)';
SELECT name, function_type FROM math_functions
WHERE function_type = 'ANALYTIC';
SELECT u.username, COUNT(mf.id) AS function_count
FROM users u
LEFT JOIN math_functions mf ON u.id = mf.owner_id
GROUP BY u.id, u.username
ORDER BY function_count DESC;

INSERT INTO math_functions (owner_id, name, function_type, definition_body)
VALUES (1, 'square', 'ANALYTIC', '{"expression": "x^2", "variables": ["x"]}'::jsonb);
INSERT INTO math_functions (owner_id, name, function_type, definition_body)
VALUES (1, 'lookup_table', 'TABULATED', '{"source_dataset_id": 5}'::jsonb);
INSERT INTO math_functions (owner_id, name, function_type, definition_body)
VALUES (1, 'composed_func', 'COMPOSITE', '{"components": [10, 11], "operation": "compose"}'::jsonb);

UPDATE math_functions
SET definition_body = '{"expression": "x^3", "variables": ["x"]}'::jsonb,
    function_type = 'ANALYTIC'
WHERE id = 7;
UPDATE math_functions
SET name = 'cube'
WHERE owner_id = 1 AND name = 'square';
UPDATE math_functions
SET definition_body = jsonb_set(definition_body, '{variables}', '["x", "a"]')
WHERE name = 'square' AND owner_id = 1;

DELETE FROM math_functions WHERE id = 7;
DELETE FROM math_functions WHERE owner_id = 1;
DELETE FROM math_functions
WHERE owner_id = 1 AND name = 'cub';