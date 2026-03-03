--liquibase formatted sql

-- ## SEQUENCES ##
--changeset liquibase:admin-202603030000-01
GRANT USAGE ON SCHEMA cron TO fdr3;
--changeset liquibase:admin-202603030000-02 endDelimiter:GO
-- cron.schedule_in_database(job_name, schedule, command, database, username, active)
SELECT cron.schedule_in_database('job_move_published_payments', '*/10 * * * *', $$call fdr3.move_published_payments(10);$$,'fdr3', 'azureuser', true);
GO