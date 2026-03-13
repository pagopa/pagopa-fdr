--liquibase formatted sql

--changeset liquibase:admin-archive-202603110001-01
INSERT INTO maintenance.partition_config (schema_name, table_name, retention_type, retention, is_active)
     VALUES ('fdr3', 'flow', 'month', 6, 'Y')
            ,('fdr3', 'payment', 'month', 6, 'Y');


--changeset liquibase:admin-archive-202603110001-02
INSERT INTO maintenance.archive_config (archive_type, src_schema_name, src_table_name, dst_schema_name, dst_table_name, is_active, batch_size, chunk_column, partition_date_column, execution_order)
     VALUES ('daily', 'remote_fdr3', 'mview_flows_published_last_day', 'fdr3', 'flow', 'Y', 50000, 'id', 'date', 1)
            ,('daily', 'remote_fdr3', 'mview_payments_published_last_day', 'fdr3', 'payment', 'Y', 1000, 'flow_id', 'flow_date', 2);