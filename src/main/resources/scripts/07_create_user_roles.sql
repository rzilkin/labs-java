CREATE TABLE IF NOT EXISTS user_roles (
    user_id integer NOT NULL,
    role_code varchar(64) NOT NULL,
    PRIMARY KEY (user_id, role_code),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_code) REFERENCES roles (code)
        ON DELETE CASCADE
);
