-- =====================================================
-- Database Reset Script
-- Drops all tables and recreates them from scratch
-- =====================================================

-- Disable foreign key checks temporarily (PostgreSQL doesn't support this,
-- but we'll drop in the correct order to handle dependencies)

BEGIN;

-- =====================================================
-- STEP 1: Drop all tables in reverse dependency order
-- =====================================================

-- Drop tables that depend on other tables first
DROP TABLE IF EXISTS dataset_points CASCADE;
DROP TABLE IF EXISTS function_components CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS tabulated_datasets CASCADE;
DROP TABLE IF EXISTS math_functions CASCADE;

-- Drop independent tables
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS performance_metrics CASCADE;

-- =====================================================
-- STEP 2: Recreate all tables in dependency order
-- =====================================================

-- 1. Create users table (no dependencies)
CREATE TABLE users (
    id serial PRIMARY KEY,
    username varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL
);

-- 2. Create roles table (no dependencies)
CREATE TABLE roles (
    code varchar(64) PRIMARY KEY,
    description varchar(255)
);

-- 3. Create math_functions table (depends on users)
CREATE TABLE math_functions (
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

-- 4. Create tabulated_datasets table (depends on math_functions)
CREATE TABLE tabulated_datasets (
    id serial PRIMARY KEY,
    function_id bigint NOT NULL,
    source_type varchar(16) NOT NULL,
    CONSTRAINT fk_tabulated_datasets_function
        FOREIGN KEY (function_id) REFERENCES math_functions (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_source_type
        CHECK (source_type IN ('MANUAL', 'GENERATED', 'DIFFERENTIATED', 'INTEGRATED'))
);

-- 5. Create dataset_points table (depends on tabulated_datasets)
CREATE TABLE dataset_points (
    dataset_id integer NOT NULL,
    point_index integer NOT NULL,
    x_value numeric NOT NULL,
    y_value numeric NOT NULL,
    PRIMARY KEY (dataset_id, point_index),
    CONSTRAINT fk_dataset_points_dataset
        FOREIGN KEY (dataset_id) REFERENCES tabulated_datasets (id)
        ON DELETE CASCADE
);

-- 6. Create function_components table (depends on math_functions)
CREATE TABLE function_components (
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

-- 7. Create user_roles table (depends on users and roles)
CREATE TABLE user_roles (
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

-- 8. Create performance_metrics table (no dependencies)
CREATE TABLE performance_metrics (
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

COMMIT;

-- =====================================================
-- Verification: List all created tables
-- =====================================================
-- Uncomment the following to verify tables were created:
-- SELECT table_name 
-- FROM information_schema.tables 
-- WHERE table_schema = 'public' 
-- ORDER BY table_name;
