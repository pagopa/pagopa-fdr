--liquibase formatted sql

-- ## CRON SCHEDULES ##
--changeset liquibase:azureuser-202603120001-01
SELECT cron.schedule_in_database
       ('update_flows_published_last_day'
        ,'00 01 * * *'
        ,$$REFRESH MATERIALIZED VIEW fdr3.mview_flows_published_last_day$$
        ,'fdr3');
SELECT cron.schedule_in_database
       ('update_payments_published_last_day'
        ,'03 01 * * *'
        ,$$REFRESH MATERIALIZED VIEW fdr3.mview_payments_published_last_day$$
        ,'fdr3');
SELECT cron.schedule_in_database
       ('execute_data_cleansing'
        ,'00 02 * * *'
        ,$$CALL maintenance.execute_data_cleansing();$$
        ,'fdr3');

-- ## CONFIGURATION ##
--changeset liquibase:azureuser-202603120001-02
INSERT INTO maintenance.retention_config (schema_name, table_name, is_active, retention_type, retention, batch_column, batch_size, retention_date_column, execution_order)
     VALUES ('maintenance', 'process_log', 'Y', 'day', 7, 'id', 5000, 'date', 1)
            ,('maintenance', 'cron_aux', 'Y', 'day', 7, 'id', 5000, 'start_time', 2)
            ,('maintenance', 'job_run_details', 'Y', 'day', 7, 'runid', 5000, 'start_time', 3) -- via postgres_fdw
            ,('fdr3', 'flow', 'Y', 'day', 35, 'id', 1000, 'date', 4); -- also cascading payment table