SELECT id FROM users;
SELECT id FROM users WHERE username = 'test_user';
SELECT username FROM users WHERE id = 1;

INSERT INTO users (username, password_hash)
VALUES ('test_user', '2d02e2878dc083aaf1eaff326b2ef163');  --md5 хеширование пароля("passqwe")
INSERT INTO users (username, password_hash)
SELECT 'bob', 'd8578edf8458ce06fbc5bb76a58c5ca4'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'bob');

UPDATE users
SET password_hash = 'ee2261129a2f5778b4a337018711d7fe'
WHERE username = 'test_user';

DELETE FROM users WHERE username = 'test_user';
DELETE FROM users WHERE id = 1;
DELETE FROM users;