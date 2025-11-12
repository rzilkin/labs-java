SELECT code FROM roles;
SELECT description FROM roles WHERE code = 'ADMIN';

INSERT INTO roles (code, description)
VALUES ('HELPER', 'Модератор');
INSERT INTO roles(code, description)
SELECT 'BAG_FINDER', 'Сотрудник по поиску багов'
WHERE NOT EXISTS(SELECT 1 FROM roles WHERE code = 'BAG_FINDER');

UPDATE roles
SET description = 'Правообладатель'
WHERE code = 'ADMIN';

DELETE FROM roles WHERE code = 'GUEST';
DELETE FROM roles;