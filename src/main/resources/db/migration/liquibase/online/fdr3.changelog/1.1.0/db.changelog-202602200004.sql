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

--changeset liquibase:202602200004-01-alter endDelimiter:GO
DO $$
    DECLARE
        schema_name TEXT := 'fdr3';
        table_name TEXT := 'payment_staging';
        modulus_val INTEGER := 32;
        i INTEGER;
        partition_name TEXT;
    BEGIN
    FOR i IN 0..(modulus_val - 1) LOOP
        partition_name := format('%s_p%s', table_name, lpad(i::text, 2, '0'));
        -- execute vacuum on the partition to update statistics and help autovacuum to make informed decisions
        -- autovacuum_vacuum_scale_factor = 0.1 -> execute vacuum when 10% of rows change (default 20%)
        -- autovacuum_analyze_scale_factor = 0.5 -> execute analyze when 5% of rows change (default 10%)
        EXECUTE format('
            ALTER TABLE %I.%I SET (
                autovacuum_vacuum_scale_factor = 0.1,
                autovacuum_analyze_scale_factor = 0.05
            );',
            schema_name, partition_name);

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
    cron_start_time timestamp with time zone,
    cron_end_time timestamp with time zone,
    CONSTRAINT cron_aux_pk PRIMARY KEY (id)
)

--changeset liquibase:202602200004-04 endDelimiter:GO
CREATE OR REPLACE PROCEDURE fdr3.move_published_payments(
    IN p_lookback_minutes integer DEFAULT 1,
    IN p_chunk_size integer DEFAULT 1000,
	IN p_start_date timestamp DEFAULT null,
	IN p_end_date timestamp DEFAULT null)
LANGUAGE plpgsql
AS $BODY$
    DECLARE
        v_start_date timestamp;
        v_end_date timestamp;
        v_job_start_time timestamp;
        v_rows_in_batch integer;
        v_total_rows_moved integer := 0;
		v_chunk_start timestamp;
        v_chunk_end timestamp;
		v_cron_aux_id bigint;
    BEGIN
        -- 1. Setup temporal range
        -- if external start and end date are provided, use them (e.g. for backfill), otherwise calculate the range based on last execution time and lookback_minutes
		IF p_start_date IS NOT NULL AND p_end_date IS NOT NULL THEN
			v_start_date := date_trunc('minute', p_start_date);
			v_end_date := date_trunc('minute', p_end_date);
        ELSE
            -- retrieve last execution time from cron_aux for this procedure
            SELECT end_time INTO v_job_start_time
            FROM maintenance.cron_aux
            WHERE procedure_name = 'move_published_payments' AND status = 'COMPLETED'
            ORDER BY id DESC LIMIT 1;

            IF v_job_start_time IS NOT NULL THEN
	            v_start_date := v_job_start_time;
	            v_end_date := v_job_start_time + (p_lookback_minutes * interval '1 minute');
            ELSE
	            v_end_date := date_trunc('minute', now());
	            v_start_date := v_end_date - (p_lookback_minutes * interval '1 minute');
            END IF;
        END IF;

		-- add log entry in cron_aux with status DRAFT to mark the start of the job and to avoid overlapping executions in case the job takes longer than the lookback window
        INSERT INTO maintenance.cron_aux(procedure_name, start_time, end_time, status,cron_start_time)
        VALUES ('move_published_payments', v_start_date, v_end_date, 'DRAFT', clock_timestamp())
        RETURNING id into v_cron_aux_id;
        COMMIT;

        -- 2. Move data (Staging -> Payment)
        LOOP
            WITH target_rows AS (
                -- select payment staging rows related to published flows in the temporal range,
                -- limit by chunk size and lock the selected rows to avoid conflicts with concurrent executions
                SELECT ps.tableoid, ps.ctid
                FROM fdr3.payment_staging ps
                JOIN fdr3.flow f ON ps.flow_id = f.id
                WHERE f.status = 'PUBLISHED'
                  AND f.published >= v_start_date
                  AND f.published < v_end_date
                LIMIT p_chunk_size
                FOR UPDATE SKIP LOCKED
            ),
            deleted_rows AS (
                DELETE FROM fdr3.payment_staging ps
                WHERE (ps.tableoid, ps.ctid) IN (SELECT tableoid, ctid FROM target_rows)
                RETURNING flow_id, iuv, iur, "index", amount,
                          pay_date, pay_status, transfer_id, created, updated
            )
            INSERT INTO fdr3.payment (
                flow_id, iuv, iur, "index", amount,
                pay_date, pay_status, transfer_id, created, updated
            )
            SELECT * FROM deleted_rows;

            GET DIAGNOSTICS v_rows_in_batch = ROW_COUNT;
            v_total_rows_moved := v_total_rows_moved + v_rows_in_batch;

           -- commit current chunk to release locks and make changes visible to other transactions
            COMMIT;

            EXIT WHEN v_rows_in_batch < p_chunk_size OR v_rows_in_batch = 0;
        END LOOP;

        -- update log entry in cron_aux with status COMPLETED and total rows moved
        UPDATE maintenance.cron_aux
        SET status = 'COMPLETED', return_message = concat('Moved: ', v_total_rows_moved), cron_end_time = clock_timestamp()
        WHERE id = v_cron_aux_id;

        COMMIT;
    END;
$BODY$;
GO

GRANT EXECUTE ON PROCEDURE fdr3.move_published_payments(integer, integer, timestamp, timestamp) TO azureuser;