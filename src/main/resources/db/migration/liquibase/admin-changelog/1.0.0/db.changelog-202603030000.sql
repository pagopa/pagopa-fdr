--liquibase formatted sql

-- ## SEQUENCES ##
--changeset liquibase:202603030000-01
-- cron.schedule_in_database(job_name, schedule, command, database, username, active)
SELECT cron.schedule_in_database('job_move_published_payments', '/01 * * * *', $$call fdr3.move_published_payments();$$,'fdr3', 'fdr3', true);