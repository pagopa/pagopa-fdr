--liquibase formatted sql

-- =============================================
--                    NOTE
-- This script MUST be executed on "postgres"
--  database in "online" PGFlex server.
-- =============================================

-- ## CRON SCHEDULES ##
--changeset liquibase:admin-archive-202603100001-01 endDelimiter:GO
-- cron.schedule_in_database(job_name, schedule, command, database, username, active)
SELECT cron.schedule_in_database
       ('job_create_partition_on_next_month'
        ,'0 0 1 * *'
        ,$$CALL maintenance.create_partition_on_next_month();$$
        ,'fdr3');
SELECT cron.schedule_in_database
       ('job_delete_expired_partitions'
        ,'0 0 1 * *'
        ,$$CALL maintenance.delete_expired_partitions();$$
        ,'fdr3');
SELECT cron.schedule_in_database
       ('job_archive_daily'
        ,'0 2 * * *'
        ,$$CALL maintenance.archive_daily();$$
        ,'fdr3');
SELECT cron.schedule_in_database
       ('job_execute_data_cleansing'
        ,'0 2 * * *'
        ,$$CALL maintenance.execute_data_cleansing();$$
        ,'fdr3');