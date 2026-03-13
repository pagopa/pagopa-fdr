--liquibase formatted sql

--changeset liquibase:admin-archive-202603110001-01 endDelimiter:GO
-- cron.schedule_in_database(job_name, schedule, command, database, username, active)
SELECT cron.schedule_in_database('xxx', '0 0 1 * *', $$call fdr3.create_partition_on_next_month();$$, 'fdr3');
GO