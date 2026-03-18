--liquibase formatted sql

-- ## CRON SCHEDULES ##
--changeset liquibase:admin-archive-202603110001-01 endDelimiter:GO
-- cron.schedule_in_database(job_name, schedule, command, database, username, active)
SELECT cron.schedule_in_database
       ('create_partition_on_next_month'
        ,'0 0 1 * *'
        ,$$CALL maintenance.create_partition_on_next_month();$$
        ,'fdr3');
SELECT cron.schedule_in_database
       ('delete_expired_partitions'
        ,'0 0 1 * *'
        ,$$CALL maintenance.delete_expired_partitions();$$
        ,'fdr3');
SELECT cron.schedule_in_database
       ('archive_daily'
        ,'00 02 * * *'
        ,$$CALL maintenance.archive_daily();$$
        ,'azureuser');
SELECT cron.schedule_in_database
       ('execute_data_cleansing'
        ,'00 02 * * *'
        ,$$CALL maintenance.execute_data_cleansing();$$
        ,'azureuser');

-- ## CONFIGURATION ##
--changeset liquibase:admin-archive-202603110001-02
INSERT INTO maintenance.partition_config (schema_name, table_name, retention_type, retention, is_active)
     VALUES ('fdr3', 'flow', 'month', 6, 'Y')
            ,('fdr3', 'payment', 'month', 6, 'Y');

--changeset liquibase:admin-archive-202603110001-03
INSERT INTO maintenance.archive_config (archive_type, src_schema_name, src_table_name, dst_schema_name, dst_table_name, is_active, batch_size, chunk_column, partition_date_column, execution_order)
     VALUES ('daily', 'remote_fdr3', 'mview_flows_published_last_day', 'fdr3', 'flow', 'Y', 50000, 'id', 'date', 1)
            ,('daily', 'remote_fdr3', 'mview_payments_published_last_day', 'fdr3', 'payment', 'Y', 1000, 'flow_id', 'flow_date', 2);

--changeset liquibase:admin-archive-202603110002-03
INSERT INTO maintenance.retention_config (schema_name, table_name, is_active, retention_type, retention, batch_column, batch_size, retention_date_column, execution_order)
     VALUES ('maintenance', 'process_log', 'Y', 'day', 7, 'id', 5000, 'date', 1)
            ,('maintenance', 'job_run_details', 'Y', 'day', 7, 'runid', 5000, 'start_time', 3) -- via postgres_fdw
            ,('fdr3', 'flow', 'Y', 'day', 35, 'id', 1000, 'date', 4); -- also cascading payment table