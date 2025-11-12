CREATE TABLE IF NOT EXISTS dataset_points (
    dataset_id integer NOT NULL,
    point_index integer NOT NULL,
    x_value numeric NOT NULL,
    y_value numeric NOT NULL,
    PRIMARY KEY (dataset_id, point_index),
    CONSTRAINT fk_dataset_points_dataset
        FOREIGN KEY (dataset_id) REFERENCES tabulated_datasets (id)
        ON DELETE CASCADE
);
