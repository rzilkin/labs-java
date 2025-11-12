SELECT id, function_id, source_type FROM tabulated_datasets;
SELECT id, source_type
FROM tabulated_datasets
WHERE function_id = 10;
SELECT td.id, mf.name
FROM tabulated_datasets td
JOIN math_functions mf ON td.function_id = mf.id
WHERE td.source_type = 'GENERATED';
SELECT
    td.id AS dataset_id,
    mf.name AS function_name,
    u.username AS owner,
    td.source_type
FROM tabulated_datasets td
JOIN math_functions mf ON td.function_id = mf.id
JOIN users u ON mf.owner_id = u.id
WHERE u.username = 'alice';

INSERT INTO tabulated_datasets (function_id, source_type)
VALUES (10, 'MANUAL');
INSERT INTO tabulated_datasets (function_id, source_type)
VALUES (15, 'GENERATED');

UPDATE tabulated_datasets
SET source_type = 'GENERATED'
WHERE function_id = 10;

DELETE FROM tabulated_datasets WHERE id = 3;
DELETE FROM tabulated_datasets WHERE function_id = 10;
