--liquibase formatted sql

-- ## SEQUENCES ##
--changeset liquibase:admin-202603030000-01
CREATE EXTENSION IF NOT EXISTS pg_cron;

--changeset liquibase:admin-202603030000-02
GRANT USAGE ON SCHEMA cron TO azureuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA cron TO azureuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA cron TO azureuser;

--changeset liquibase:admin-202603030000-03 endDelimiter:GO
-- cron.schedule(job_name, schedule, command, database, username, active)
SELECT cron.schedule('job_move_published_payments', '*/10 * * * *', $$call fdr3.move_published_payments(10);$$);
GO