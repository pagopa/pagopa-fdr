--liquibase formatted sql

-- ## SEQUENCES ##
--changeset liquibase:azureuser-202603110001-01
CREATE SEQUENCE IF NOT EXISTS maintenance.log_sequence
       INCREMENT 1
       START 1
       MINVALUE 1
       MAXVALUE 9223372036854775807
       CACHE 1;

-- ## TABLES ##
--changeset liquibase:azureuser-202603110001-02
CREATE TABLE IF NOT EXISTS maintenance.process_log (
    id BIGINT DEFAULT nextval('maintenance.log_sequence'::regclass) NOT NULL,
    date TIMESTAMP without time zone NOT NULL,
    execution_id CHARACTER VARYING(50) NOT NULL,
    "user" CHARACTER VARYING(50) NOT NULL,
    process CHARACTER VARYING(100) NOT NULL,
    step CHARACTER VARYING(50) NOT NULL,
    outcome CHARACTER VARYING(16),
    note CHARACTER VARYING,
    statement CHARACTER VARYING,
    CONSTRAINT process_log_pk PRIMARY KEY (id)
);
COMMENT ON TABLE maintenance.process_log
        IS 'Table containing all log entries generated during maintenance processes';

CREATE TABLE IF NOT EXISTS maintenance.retention_config (
    schema_name CHARACTER VARYING(50) NOT NULL,
    table_name CHARACTER VARYING(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT false,
    retention_type CHARACTER VARYING(10) NOT NULL DEFAULT 'day',
    retention INTEGER NOT NULL,
    batch_column VARCHAR(50) NOT NULL DEFAULT 'id',
    batch_size INTEGER NOT NULL DEFAULT 50000,
    retention_date_column VARCHAR(50) NOT NULL DEFAULT 'date',
    execution_order INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT retention_config_pk PRIMARY KEY (schema_name, table_name)
);
COMMENT ON TABLE maintenance.retention_config
        IS 'Table containing all information about configuration for data retention based on date';
