--liquibase formatted sql

-- ## ALTER TABLE FLOW ##
--changeset liquibase:202602200002-01
ALTER TABLE fdr3.flow ALTER COLUMN id TYPE bigint;

ALTER TABLE fdr3.flow ALTER COLUMN revision TYPE bigint;

ALTER TABLE fdr3.flow ALTER COLUMN tot_payments TYPE bigint;

ALTER TABLE fdr3.flow ALTER COLUMN tot_amount TYPE numeric(19,2);

ALTER TABLE fdr3.flow ALTER COLUMN computed_tot_payments TYPE bigint;

ALTER TABLE fdr3.flow ALTER COLUMN computed_tot_amount TYPE numeric(19,2);

ALTER TABLE fdr3.flow SET (fillfactor = 75);