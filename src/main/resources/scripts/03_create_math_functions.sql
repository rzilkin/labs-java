CREATE TABLE IF NOT EXISTS math_functions (
    id serial PRIMARY KEY,
    owner_id integer NOT NULL,
    name varchar(255) NOT NULL,
    function_type varchar(16) NOT NULL,
    definition_body jsonb NOT NULL,
    CONSTRAINT fk_math_functions_owner
        FOREIGN KEY (owner_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_function_type
        CHECK (function_type IN ('ANALYTIC', 'TABULATED', 'COMPOSITE')),
    CONSTRAINT uq_owner_name UNIQUE (owner_id, name)
);
