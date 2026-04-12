--liquibase formatted sql

-- ## CONFIGURATION ##
--changeset liquibase:azureuser-202603110004-01
INSERT INTO maintenance.retention_config (schema_name, table_name, is_active, retention_type, retention, batch_column, batch_size, retention_date_column, execution_order)
     VALUES ('maintenance', 'process_log', 'Y', 'day', 7, 'id', 5000, 'date', 1)
            ,('maintenance', 'cron_aux', 'Y', 'day', 7, 'id', 5000, 'start_time', 2)
            ,('maintenance', 'job_run_details', 'Y', 'day', 7, 'runid', 5000, 'start_time', 3) -- via postgres_fdw
            ,('fdr3', 'flow', 'Y', 'day', 35, 'id', 1000, 'date', 4); -- also cascading payment table