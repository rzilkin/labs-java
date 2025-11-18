SELECT role_code FROM user_roles WHERE user_id = 1;
SELECT r.code, r.description
FROM user_roles ur
JOIN roles r ON ur.role_code = r.code
WHERE ur.user_id = 1;
SELECT r.code, r.description
FROM user_roles ur
JOIN roles r ON ur.role_code = r.code
JOIN users u ON ur.user_id = u.id
WHERE u.username = 'alice';
SELECT u.id, u.username
FROM user_roles ur
JOIN users u ON ur.user_id = u.id
WHERE ur.role_code = 'ADMIN';

INSERT INTO user_roles (user_id, role_code)
VALUES (1, 'ADMIN');
INSERT INTO user_roles (user_id, role_code)
VALUES
    (1, 'USER'),
    (1, 'ADMIN');
INSERT INTO user_roles (user_id, role_code)
SELECT u.id, 'HELPER'
FROM users u
WHERE u.username = 'bob';

UPDATE user_roles
SET role_code = 'USER'
WHERE role_code = 'GUEST';

DELETE FROM user_roles
WHERE user_id = 1 AND role_code = 'ADMIN';
DELETE FROM user_roles
WHERE user_id = 1;
DELETE FROM user_roles
WHERE role_code = 'TESTER';