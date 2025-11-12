CREATE TABLE IF NOT EXISTS function_components (
    composite_id integer NOT NULL,
    component_id integer NOT NULL,
    position smallint NOT NULL,
    PRIMARY KEY (composite_id, position),
    CONSTRAINT fk_function_components_composite
        FOREIGN KEY (composite_id) REFERENCES math_functions (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_function_components_component
        FOREIGN KEY (component_id) REFERENCES math_functions (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_composite_component UNIQUE (composite_id, component_id),
    CONSTRAINT chk_position_positive CHECK (position > 0)
);
