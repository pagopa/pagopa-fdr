--liquibase formatted sql

--changeset liquibase:admin-202603100001 endDelimiter:GO
-- cron.schedule_in_database(job_name, schedule, command, database, username, active)
SELECT cron.schedule_in_database('xxx', '0 0 1 * *', $$call fdr3.create_partition_on_next_month();$$, 'fdr3');
GO