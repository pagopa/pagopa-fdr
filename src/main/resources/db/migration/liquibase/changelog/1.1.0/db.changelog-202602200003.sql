--liquibase formatted sql

-- ## ALTER TABLE PAYMENT ##
--changeset liquibase:202602200003-01
--ALTER TABLE fdr3.payment ALTER COLUMN id TYPE bigint; -- commented because it will be dropped later

ALTER TABLE fdr3.payment ALTER COLUMN flow_id TYPE bigint;

ALTER TABLE fdr3.payment ALTER COLUMN iuv TYPE character varying(35) COLLATE pg_catalog."default";

ALTER TABLE fdr3.payment ALTER COLUMN iur TYPE character varying(35) COLLATE pg_catalog."default";

ALTER TABLE fdr3.payment ALTER COLUMN index TYPE bigint;

ALTER TABLE fdr3.payment ALTER COLUMN transfer_id TYPE bigint;

ALTER TABLE fdr3.payment ALTER COLUMN amount TYPE numeric(19,2);

--changeset liquibase:202602200003-02
ALTER TABLE fdr3.payment DROP CONSTRAINT payment_pk; -- drop primary key constraint

ALTER TABLE fdr3.payment ADD PRIMARY KEY (flow_id, index); -- add new primary key constraint

ALTER TABLE fdr3.payment DROP COLUMN id; -- drop the old id column

DROP SEQUENCE IF EXISTS fdr3.payment_sequence; -- drop the sequence

DROP INDEX IF EXISTS fdr3.payment_by_fdr_idx; -- drop the index on flow_id, as it is now part of the primary key