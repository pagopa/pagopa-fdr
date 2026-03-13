--liquibase formatted sql

-- ## SEQUENCES ##
--changeset liquibase:archive-azureuser-202603090002-01
CREATE SEQUENCE IF NOT EXISTS maintenance.log_sequence
       INCREMENT 1
       START 1
       MINVALUE 1
       MAXVALUE 9223372036854775807
       CACHE 1;

-- ## TABLES ##
--changeset liquibase:archive-azureuser-202603090002-02
CREATE TABLE IF NOT EXISTS maintenance.process_log (
    id BIGINT DEFAULT nextval('log_sequence'::regclass) NOT NULL,
    date TIMESTAMP without time zone NOT NULL,
    execution_id CHARACTER VARYING(50) NOT NULL,
    "user" CHARACTER VARYING(50) NOT NULL,
    process CHARACTER VARYING(100) NOT NULL,
    step CHARACTER VARYING(50) NOT NULL,
    outcome CHARACTER VARYING(16),
    note CHARACTER VARYING,
    CONSTRAINT process_log_pk PRIMARY KEY (id)
);
COMMENT ON TABLE maintenance.partition_status
        IS 'Table containing all log entries generated during maintenance processes';

CREATE TABLE IF NOT EXISTS maintenance.partition_config (
    schema_name CHARACTER VARYING(50) NOT NULL,
    table_name CHARACTER VARYING(50) NOT NULL,
    retention_type CHARACTER VARYING(10) NOT NULL,
    retention INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT partition_config_pk PRIMARY KEY (schema_name, table_name)
);
COMMENT ON TABLE maintenance.partition_config
        IS 'Table containing all information about partition configuration';

CREATE TABLE IF NOT EXISTS maintenance.partition_status (
    schema_name CHARACTER VARYING(50) NOT NULL,
    table_name CHARACTER VARYING(50) NOT NULL,
    partition_name CHARACTER VARYING(50) NOT NULL,
    status character varying(2),
    inserted_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    deleted_at timestamp(6) without time zone,
    CONSTRAINT partition_status_pk PRIMARY KEY (schema_name, table_name, partition_name)
);
COMMENT ON TABLE maintenance.partition_status
        IS 'Table containing all information about partition current status';

CREATE TABLE IF NOT EXISTS maintenance.archive_config (
    archive_type CHARACTER VARYING(10) NOT NULL DEFAULT 'daily',
    src_schema_name CHARACTER VARYING(50) NOT NULL,
    src_table_name CHARACTER VARYING(50) NOT NULL,
    dst_schema_name CHARACTER VARYING(50) NOT NULL,
    dst_table_name CHARACTER VARYING(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT false,
    batch_column VARCHAR(50) NOT NULL DEFAULT 'id',
    batch_size INTEGER NOT NULL DEFAULT 50000,
    partition_date_column VARCHAR(50) NOT NULL DEFAULT 'date',
    execution_order INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT archive_config_pk PRIMARY KEY (archive_type, src_schema_name, src_table_name)
);
COMMENT ON TABLE maintenance.archive_config
        IS 'Table containing all information about data archive (based on date RANGE) configuration';