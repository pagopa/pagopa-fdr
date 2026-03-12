--liquibase formatted sql

-- ## EXTENSIONS ##
--changeset liquibase:admin-archive-202603090001-01
CREATE EXTENSION IF NOT EXISTS pg_cron;
