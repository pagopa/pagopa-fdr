--liquibase formatted sql

-- ## EXTENSIONS ##
--changeset liquibase:archive-azureuser-202603090001-01
CREATE EXTENSION IF NOT EXISTS pg_cron;
