--liquibase formatted sql

-- ## ALTER TABLE FLOW_TO_HISTORY ##
--changeset liquibase:202602200001-01
ALTER TABLE fdr3.flow_to_history ALTER COLUMN id TYPE bigint;

ALTER TABLE fdr3.flow_to_history ALTER COLUMN revision TYPE bigint;

ALTER TABLE fdr3.flow_to_history ALTER COLUMN retries TYPE bigint;