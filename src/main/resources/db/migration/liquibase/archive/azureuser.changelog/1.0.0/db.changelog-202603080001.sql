--liquibase formatted sql

-- =============================================
--                    NOTE
-- This script MUST be executed on "postgres"
--  database in "archive" PGFlex server.
-- =============================================

-- ## EXTENSIONS ##
--changeset liquibase:archive-azureuser-202603080001-01
CREATE EXTENSION IF NOT EXISTS pg_cron;