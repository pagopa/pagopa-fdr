--liquibase formatted sql

-- ## TABLE PAYMENT_STAGING ##
--changeset liquibase:202602200004-01 endDelimiter:GO
DO $$
DECLARE
    schema_name TEXT := 'fdr3';
    table_name TEXT := 'payment_staging';
    modulus_val INTEGER := 32;
    i INTEGER;
    partition_name TEXT;
BEGIN
    -- 1. Create master table
EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.%I (
            flow_id bigint NOT NULL,
            index bigint NOT NULL,
            org_id character varying(15) NOT NULL,
            iuv character varying(35) NOT NULL,
            iur character varying(35) NOT NULL,
            amount numeric(19,2) NOT NULL,
            pay_date timestamp(6) NOT NULL,
            pay_status character varying(50) NOT NULL,
            transfer_id bigint NOT NULL,
            created timestamp(6),
            updated timestamp(6),
            CONSTRAINT payment_pk PRIMARY KEY (flow_id, index, org_id)
        ) PARTITION BY HASH (org_id);',
               schema_name, table_name);

-- 2. Create partitions
FOR i IN 0..(modulus_val - 1) LOOP
        -- format naming as payment_staging_p01, payment_staging_p02...
        partition_name := format('%s_p%s', table_name, lpad(i::text, 2, '0'));

EXECUTE format('
            CREATE TABLE IF NOT EXISTS %I.%I
            PARTITION OF %I.%I
            FOR VALUES WITH (MODULUS %s, REMAINDER %s);',
               schema_name, partition_name, schema_name, table_name, modulus_val, i);
END LOOP;
END $$;
GO

--changeset liquibase:202602200004-02
CREATE OR REPLACE VIEW fdr3.payment_full_view AS
    -- 1. extract data from payment
    SELECT
        flow_id,
        index,
        iuv,
        iur,
        amount,
        pay_date,
        pay_status,
        transfer_id,
        created,
        updated,
        'FINAL'::text AS record_origin
    FROM fdr3.payment

UNION ALL

    -- 2. extract data from payment_staging
    SELECT
        flow_id,
        index,
        iuv,
        iur,
        amount,
        pay_date,
        pay_status,
        transfer_id,
        created,
        updated,
        'STAGING'::text AS record_origin
    FROM fdr3.payment_staging;

--changeset liquibase:202602200004-03 endDelimiter:GO
CREATE OR REPLACE PROCEDURE fdr3.move_published_payments(
    p_start_date timestamp,
    p_end_date timestamp
)
LANGUAGE plpgsql
AS $$
    DECLARE
        rows_moved integer;
    BEGIN
    -- use CTE to identify, delete and insert in one shot
    WITH
        target_flows AS (
            -- identify flow ids published in a specified temporal range
            SELECT id FROM fdr3.flow
            WHERE status = 'PUBLISHED'
                AND published >= p_start_date
                AND published <= p_end_date
        ),
        deleted_rows AS (
            -- delete from payment_staging rows belonging to published flows
            DELETE FROM fdr3.payment_staging
                WHERE flow_id IN (SELECT id FROM target_flows)
            RETURNING
                flow_id, iuv, iur, index, amount,
                pay_date, pay_status, transfer_id, created, updated
        )
    -- insert row in the payment table
    INSERT INTO fdr3.payment (
        flow_id, iuv, iur, index, amount,
        pay_date, pay_status, transfer_id, created, updated
    )
    SELECT * FROM deleted_rows;

    GET DIAGNOSTICS rows_moved = ROW_COUNT;
    RAISE NOTICE 'Move completed: % payments moved to payment table in range between % and %', rows_moved, p_start_date, p_end_date;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE EXCEPTION 'Error during moving items: %', SQLERRM;
    END;
$$;
GO