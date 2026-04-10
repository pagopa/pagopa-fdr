--liquibase formatted sql

-- =============================================
--                    NOTE
-- This script MUST be executed on "postgres"
--  database in "online" PGFlex server.
-- =============================================

-- ## CRON SCHEDULES ##
--changeset liquibase:azureuser-202603120001-01
SELECT cron.schedule_in_database
       ('update_flows_published_last_day'
        ,'30 0 * * *'
        ,$$REFRESH MATERIALIZED VIEW fdr3.mview_flows_published_last_day$$
        ,'fdr3');
SELECT cron.schedule_in_database
       ('update_payments_published_last_day'
        ,'0 1 * * *'
        ,$$REFRESH MATERIALIZED VIEW fdr3.mview_payments_published_last_day$$
        ,'fdr3');
SELECT cron.schedule_in_database
       ('execute_data_cleansing'
        ,'0 3 * * *'
        ,$$CALL maintenance.execute_data_cleansing()$$
        ,'fdr3');
