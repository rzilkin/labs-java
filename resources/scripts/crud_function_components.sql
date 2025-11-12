SELECT
    position,
    component_id,
    mf.name AS component_name,
    mf.function_type
FROM function_components fc
JOIN math_functions mf ON fc.component_id = mf.id
WHERE fc.composite_id = 20
ORDER BY fc.position;
SELECT
    u.username AS owner,
    parent.name AS composite_function,
    fc.position,
    child.name AS component_name,
    child.function_type
FROM function_components fc
JOIN math_functions parent ON fc.composite_id = parent.id
JOIN math_functions child  ON fc.component_id = child.id
JOIN users u ON parent.owner_id = u.id
WHERE parent.name = 'f_composed'
ORDER BY fc.position;
SELECT EXISTS (SELECT 1 FROM function_components WHERE composite_id = 20);

INSERT INTO function_components (composite_id, component_id, position)
VALUES (20, 10, 1);
INSERT INTO function_components (composite_id, component_id, position)
VALUES
    (20, 10, 1),
    (20, 11, 2),
    (20, 12, 3);

UPDATE function_components
SET position = CASE
    WHEN component_id = 10 THEN 2
    WHEN component_id = 11 THEN 1
END
WHERE composite_id = 20 AND component_id IN (10, 11);
UPDATE function_components
SET component_id = 13
WHERE composite_id = 20 AND position = 1;

DELETE FROM function_components
WHERE composite_id = 20 AND position = 1;
DELETE FROM function_components
WHERE composite_id = 20 AND component_id = 10;
DELETE FROM function_components
WHERE composite_id = 20;