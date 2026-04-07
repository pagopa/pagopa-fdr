--liquibase formatted sql

-- ## CONFIGURATION ##
--changeset liquibase:archive-azureuser-202603090008-01
INSERT INTO maintenance.partition_config (schema_name, table_name, retention_type, retention, is_active)
     VALUES ('fdr3', 'flow', 'month', 6, 'Y')
            ,('fdr3', 'payment', 'month', 6, 'Y');

--changeset liquibase:archive-azureuser-202603090008-02
INSERT INTO maintenance.archive_config (archive_type, src_schema_name, src_table_name, dst_schema_name, dst_table_name, is_active, batch_size, batch_column, partition_date_column, execution_order)
     VALUES ('daily', 'remote_online_db', 'mview_flows_published_last_day', 'fdr3', 'flow', 'Y', 50000, 'id', 'date', 1)
            ,('daily', 'remote_online_db', 'mview_payments_published_last_day', 'fdr3', 'payment', 'Y', 1, 'flow_id', 'flow_date', 2); -- batching by all payments in flow_id

--changeset liquibase:archive-azureuser-202603090008-03
INSERT INTO maintenance.retention_config (schema_name, table_name, is_active, retention_type, retention, batch_column, batch_size, retention_date_column, execution_order)
     VALUES ('maintenance', 'process_log', 'Y', 'day', 7, 'id', 5000, 'date', 1)
            ,('maintenance', 'job_run_details', 'Y', 'day', 7, 'runid', 5000, 'start_time', 2); -- via postgres_fdw