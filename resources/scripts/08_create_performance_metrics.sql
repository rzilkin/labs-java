CREATE TABLE IF NOT EXISTS performance_metrics (
    id serial PRIMARY KEY,
    engine varchar(32) NOT NULL,
    operation varchar(255) NOT NULL,
    records_processed integer NOT NULL,
    elapsed_ms integer NOT NULL,
    CONSTRAINT chk_engine
        CHECK (engine IN ('MANUAL_JDBC', 'FRAMEWORK_ORM')),
    CONSTRAINT chk_records_processed_non_negative
        CHECK (records_processed >= 0),
    CONSTRAINT chk_elapsed_ms_non_negative
        CHECK (elapsed_ms >= 0)
);
