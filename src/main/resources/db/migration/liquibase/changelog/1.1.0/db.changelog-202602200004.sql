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

--changeset liquibase:202602200004-03
CREATE SEQUENCE IF NOT EXISTS maintenance.cron_aux_sequence
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
CREATE TABLE IF NOT EXISTS maintenance.cron_aux
(
    id bigint DEFAULT nextval('maintenance.cron_aux_sequence'::regclass) NOT NULL,
    procedure_name character varying(100) COLLATE pg_catalog."default",
    start_time timestamp with time zone,
    end_time timestamp with time zone,
    status character varying(20),
    return_message text COLLATE pg_catalog."default",
    CONSTRAINT cron_aux_pk PRIMARY KEY (id)
)

--changeset liquibase:202602200004-04 endDelimiter:GO
CREATE OR REPLACE PROCEDURE fdr3.move_published_payments(
    IN p_lookback_minutes integer DEFAULT 1)
LANGUAGE 'plpgsql'
    SECURITY DEFINER
    SET search_path=fdr3, public, pg_temp
AS $BODY$
    DECLARE
        v_start_date timestamp;
        v_end_date timestamp;
        v_job_start_time timestamp;
        rows_moved integer;
    BEGIN
        -- 1. Retrieve last execution time from cron_aux for this procedure
        SELECT end_time
        INTO v_job_start_time
        FROM maintenance.cron_aux
        WHERE procedure_name = 'move_published_payments' AND status = 'COMPLETED'
        ORDER BY id DESC LIMIT 1;

        -- 2. Check start and end time
        IF v_job_start_time IS NOT NULL THEN
            -- if entry exists and start_time is not null, use it as reference for the new window
            v_start_date := v_job_start_time;
            v_end_date := v_job_start_time + (p_lookback_minutes * interval '1 minute');
			IF v_end_date > now() THEN
			   -- if end_date is in the future, raise an exception to avoid processing incomplete data
			   RAISE EXCEPTION 'v_end_date > now()';
            END IF;
            RAISE NOTICE 'Time retrieved from cron_aux: % - %', v_start_date, v_end_date;
        ELSE
            -- fallback if no entry exitst or start_time is null, use now as reference for the new window
            v_end_date := date_trunc('minute', now());
            v_start_date := v_end_date - (p_lookback_minutes * interval '1 minute');
            RAISE NOTICE 'No entry in cron_aux. Time based on now: % - %', v_start_date, v_end_date;
        END IF;

        -- 3. Move data (Staging -> Payment)
        WITH
            target_flows AS (
                SELECT id FROM fdr3.flow
                WHERE status = 'PUBLISHED'
                  AND published >= v_start_date
                  AND published < v_end_date
            ),
            deleted_rows AS (
        DELETE FROM fdr3.payment_staging
        WHERE flow_id IN (SELECT id FROM target_flows)
            RETURNING
                            flow_id, iuv, iur, "index", amount,
                            pay_date, pay_status, transfer_id, created, updated
                    )
        INSERT INTO fdr3.payment (
            flow_id, iuv, iur, "index", amount,
            pay_date, pay_status, transfer_id, created, updated
        )
        SELECT * FROM deleted_rows;

        GET DIAGNOSTICS rows_moved = ROW_COUNT;

        -- 4. Add execution into cron_aux
        INSERT INTO maintenance.cron_aux(procedure_name, start_time, end_time, status, return_message)
            VALUES ('move_published_payments', v_start_date, v_end_date, 'COMPLETED', concat('payment moved from staging: ',rows_moved));

        RAISE NOTICE 'Move completed: % record moved from staging in the range % - %', rows_moved, v_start_date, v_end_date;

    EXCEPTION
		WHEN OTHERS THEN
			INSERT INTO maintenance.cron_aux(procedure_name, start_time, end_time, status, return_message)
			    VALUES ('move_published_payments', v_start_date, v_end_date, 'ERROR', SQLERRM);
            RAISE WARNING 'Error occurred during moving data: %', SQLERRM;
    END;
$BODY$;
GO

GRANT EXECUTE ON PROCEDURE fdr3.move_published_payments(integer) TO azureuser;