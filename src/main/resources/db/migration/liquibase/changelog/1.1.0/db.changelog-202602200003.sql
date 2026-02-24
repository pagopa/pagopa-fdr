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
ALTER TABLE fdr3.payment DROP CONSTRAINT IF EXISTS payment_pk; -- drop primary key constraint

--ALTER TABLE fdr3.payment ADD PRIMARY KEY (flow_id, "index"); -- add new primary key constraint
DO $$
BEGIN
    -- check if exists already a primary key for the table
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'fdr3'
          AND table_name = 'payment'
          AND constraint_type = 'PRIMARY KEY'
    )
       THEN
            ALTER TABLE fdr3.payment ADD PRIMARY KEY (flow_id, "index");
    ELSE
            RAISE NOTICE 'Primary key already exists... skipping.';
    END IF;
END $$;

ALTER TABLE fdr3.payment DROP COLUMN IF EXISTS id; -- drop the old id column

DROP SEQUENCE IF EXISTS fdr3.payment_sequence; -- drop the sequence

DROP INDEX IF EXISTS fdr3.payment_by_fdr_idx; -- drop the index on flow_id, as it is now part of the primary key