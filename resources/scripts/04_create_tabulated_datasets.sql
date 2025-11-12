CREATE TABLE IF NOT EXISTS tabulated_datasets (
    id serial PRIMARY KEY,
    function_id bigint NOT NULL,
    source_type varchar(16) NOT NULL,
    CONSTRAINT fk_tabulated_datasets_function
        FOREIGN KEY (function_id) REFERENCES math_functions (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_source_type
        CHECK (source_type IN ('MANUAL', 'GENERATED', 'DIFFERENTIATED', 'INTEGRATED'))
);
