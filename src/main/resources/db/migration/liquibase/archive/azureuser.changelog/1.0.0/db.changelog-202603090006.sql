--liquibase formatted sql

--changeset liquibase:archive-azureuser-202603090006-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.execute_daily_archive_on_table(
    IN p_schema_name TEXT,
    IN p_table_name TEXT,
    IN p_batch_size BIGINT DEFAULT NULL,
    IN p_min_id BIGINT DEFAULT NULL,
    IN p_max_id BIGINT DEFAULT NULL,
    IN p_start_date DATE DEFAULT NULL,
    IN p_end_date DATE DEFAULT NULL,
    IN p_execution_user TEXT DEFAULT NULL,
    IN p_execution_id TEXT DEFAULT NULL
)
AS $function$
DECLARE

    l_execution_user TEXT := COALESCE(p_execution_user, SESSION_USER);
    l_process_name TEXT := 'execute_daily_archive_on_table';
    l_execution_id TEXT := COALESCE(p_execution_id, Gen_random_uuid()::TEXT);

    l_step TEXT := 'START';
    l_status TEXT := 'OK';
    l_record RECORD;
    l_process_log_id BIGINT;

    l_current_start_id BIGINT;
    l_current_end_id BIGINT;
    l_batch_rows BIGINT;
    l_archived_batches BIGINT := 0;
    l_archived_records BIGINT := 0;

BEGIN

    -- Log start process
    BEGIN
        INSERT INTO maintenance.process_log(
                     "date"
                     ,execution_id
                     ,"user"
                     ,process
                     ,step
                     ,outcome)
             VALUES (Clock_timestamp()
                     ,execution_id
                     ,execution_user
                     ,process_name
                     ,l_step
                     ,l_status);
    END;

    -- Retrieve archiving configuration for required table
    SELECT src_schema_name
           ,src_table_name
           ,dst_schema_name
           ,dst_table_name
           ,batch_size
           ,batch_column
           ,partition_date_column
      INTO l_record
      FROM maintenance.archive_config
     WHERE src_schema_name = p_schema_name
           AND src_table_name = p_table_name
           AND is_active IS TRUE
           AND Lower(archive_type) = 'daily'
     LIMIT 1;

    IF l_record IS NULL
    THEN

        RAISE NOTICE 'No data found to archive for table [%.%]', l_record.src_schema_name, l_record.src_table_name;
        l_step := 'ARCHIVING_DATA';
        l_status := 'SKIPPED';
        INSERT INTO maintenance.process_log(
                     "date"
                     ,execution_id
                     ,"user"
                     ,process
                     ,step
                     ,outcome
                     ,note)
             VALUES (Clock_timestamp()
                     ,l_execution_id
                     ,l_execution_user
                     ,l_process_name
                     ,l_step
                     ,l_status
                     ,Concat('Table: [', l_record.src_schema_name, '.', l_record.src_table_name, '], Total records moved: [0]'));
    ELSE

        -- Defining main section for "retrieve boundaries on batch columns" query
        l_stmt := Format('
            SELECT MIN(%I), MAX(%I)
              FROM %I.%I
        ', l_record.batch_column, l_record.batch_column, l_record.src_schema_name, l_record.src_table_name);

        -- Defining optional filters section for "retrieve boundaries on batch columns" query
        IF p_start_date IS NOT NULL AND p_end_date IS NOT NULL
        THEN
            l_stmt := l_stmt || format('
                 WHERE %I >= $1 AND %I < $2
            ', l_record.batch_column, l_record.batch_column);
            EXECUTE l_stmt INTO l_min_id, l_max_id USING p_min_id, p_max_id;
        ELSE
            EXECUTE l_stmt INTO l_min_id, l_max_id;
        END IF;

        -- Setting batch column boundaries, choosing by the one passed on CALL instruction or the ones found by query
        l_min_id := COALESCE(p_min_id, l_min_id);
        l_max_id := COALESCE(p_max_id, l_max_id);
        RAISE NOTICE 'Scanning through table [%.%] on column [%] from [%] to [%], with batches of size [%]',
                l_record.src_schema_name, l_record.src_table_name, l_record.batch_column, l_min_id, l_max_id, l_record.batch_size;

        -- Retrieve (possible) partition names using the value defined by the partition_date_column column
        l_current_end_id := l_current_start_id + l_record.batch_size;
        l_stmt := Format('
            SELECT Array_agg(DISTINCT %L || ''_p'' || To_char(Date_trunc(''month'', "%I"), ''YYYYMM''))
              FROM %I.%I
             WHERE %I >= $1
                   AND %I < $2
        ', l_record.dst_table_name, l_record.partition_date_column, l_record.src_schema_name, l_record.src_table_name, l_record.batch_column, l_record.batch_column);
        EXECUTE l_stmt INTO l_partition_names USING l_current_start_id, l_current_end_id;

        -- Creating process_log record and using the generated ID in order to update the same record
        BEGIN
            l_step := 'START_DATA_ARCHIVING';
            l_status := 'ONGOING';
            INSERT INTO maintenance.process_log(
                         "date"
                         ,execution_id
                         ,"user"
                         ,process
                         ,step
                         ,outcome
                         ,note)
                 VALUES (Clock_timestamp()
                         ,execution_id
                         ,execution_user
                         ,process_name
                         ,l_step
                         ,l_status
                         ,Concat('Table: [', l_record.src_schema_name, '.', l_record.src_table_name, ']'))
              RETURNING id
                        INTO l_process_log_id;
        END;

        l_archived_records := 0;
        l_current_start_id := l_min_id;
        l_step := 'ARCHIVING_DATA';

        WHILE l_current_start_id <= l_max_id
        LOOP

            -- Archiving data in batches from source table to destination table
            l_stmt := Format('
                INSERT INTO %I.%I
                SELECT *
                  FROM %I.%I
                 WHERE %I >= $1
                       AND %I < $2
                    ON CONFLICT DO NOTHING
            ', l_record.dst_schema_name, l_record.dst_table_name, l_record.src_schema_name, l_record.src_table_name, l_record.batch_column, l_record.batch_column);
            EXECUTE l_stmt USING l_current_start_id, l_current_end_id;

            -- Calculating statistics
            GET DIAGNOSTICS l_batch_rows = ROW_COUNT;
            l_archived_records := l_archived_records + l_batch_rows;
            l_archived_batches := l_archived_batches + 1;

            --
            l_current_start_id := l_current_end_id;
            UPDATE maintenance.partition_status
               SET "status" = 'U'
                   ,updated_at = Clock_timestamp()
             WHERE schema_name = l_record.dst_schema_name
                   AND table_name = l_record.dst_table_name
                   AND partition_name = ANY(l_partition_names);

            -- Update the same process_log record with updated info, setting date with current timestamp
            UPDATE maintenance.process_log
               SET "date" = Clock_timestamp()
                   ,note = Concat(
                       'Table: [', l_record.src_schema_name, '.', l_record.src_table_name,
                       '], Archived batches: [', l_archived_batches,
                       '], Archived records: [', l_archived_records, ']')
             WHERE id = l_process_log_id;

        END LOOP;

        -- Execute ANALYZE command on archive table
        IF l_archived_records > 0
        THEN
            EXECUTE Format('
                ANALYZE %I.%I
            ', l_record.dst_schema_name, l_record.dst_table_name);
            COMMIT;
        END IF;

        -- Update the same process_log record one last time with final info
        l_step := 'DATA_ARCHIVED';
        l_status := 'OK';
        UPDATE maintenance.process_log
           SET "date" = Clock_timestamp()
               ,step = l_step
               ,status = l_status
               ,note = Concat(
                   'Table: [', l_record.src_schema_name, '.', l_record.src_table_name,
                   '], Archived batches: [', l_archived_batches,
                   '], Archived records: [', l_archived_records, ']')
         WHERE id = l_process_log_id;

    END IF;

     -- Log end process
     BEGIN
        l_step := 'END';
        l_status := 'OK';
        INSERT INTO maintenance.process_log(
                     "date"
                     ,execution_id
                     ,"user"
                     ,process
                     ,step
                     ,outcome)
             VALUES (Clock_timestamp()
                     ,execution_id
                     ,execution_user
                     ,process_name
                     ,l_step
                     ,l_status);
     END;

EXCEPTION WHEN OTHERS THEN

    -- Update the same process_log record one last time with error info
    l_status := 'KO';
    l_step := 'DATA_ARCHIVED';
    UPDATE maintenance.process_log
       SET "date" = Clock_timestamp()
           ,step = l_step
           ,status = l_status
           ,note = Concat(
               'Table: [', l_record.src_schema_name, '.', l_record.src_table_name,
               '], Archived batches: [', l_archived_batches,
               '], Archived records: [', l_archived_records,
               '], Error: ', SQLERRM)
     WHERE id = l_process_log_id;

    -- Log end process
    l_step := 'END';
    INSERT INTO maintenance.process_log(
                 "date"
                 ,execution_id
                 ,"user"
                 ,process
                 ,step
                 ,outcome)
         VALUES (Clock_timestamp()
                 ,execution_id
                 ,execution_user
                 ,process_name
                 ,l_step
                 ,l_status);

END;
$function$ LANGUAGE 'plpgsql'
GO


--changeset liquibase:archive-azureuser-202603090006-02 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.execute_daily_archive()
AS $function$
DECLARE

    l_execution_user TEXT := SESSION_USER;
    l_process_name TEXT := 'execute_daily_archive';
    l_execution_id TEXT := Gen_random_uuid()::TEXT;

    l_step TEXT := 'START';
    l_status TEXT := 'OK';
    l_record RECORD;

BEGIN

    -- Log start process
    BEGIN
        INSERT INTO maintenance.process_log(
                     "date"
                     ,execution_id
                     ,"user"
                     ,process
                     ,step
                     ,outcome)
             VALUES (Clock_timestamp()
                     ,l_execution_id
                     ,l_execution_user
                     ,l_process_name
                     ,l_step
                     ,l_status);
    END;

    FOR l_record IN
           SELECT src_schema_name
                  ,src_table_name
                  ,dst_schema_name
                  ,dst_table_name
                  ,batch_size
                  ,partition_date_column
             FROM maintenance.archive_config
            WHERE is_active IS TRUE
                  AND Lower(archive_type) = 'daily'
            ORDER BY execution_order ASC
    LOOP

        RAISE NOTICE 'Archiving data from [%.%] to [%.%]', l_record.src_schema_name, l_record.src_table_name, l_record.dst_schema_name, l_record.dst_table_name;
        CALL maintenance.xx(
            p_schema_name => l_record.src_schema_name,
            p_table_name => l_record.table_name,
            p_batch_size => l_batch_size,
            p_execution_user => l_execution_user,
            p_execution_id => l_execution_id
        );

    END LOOP;


    -- Log end process
    l_step := 'END';
    INSERT INTO maintenance.process_log(
                 "date"
                 ,execution_id
                 ,"user"
                 ,process
                 ,step
                 ,outcome)
         VALUES (Clock_timestamp()
                 ,l_execution_id
                 ,l_execution_user
                 ,l_process_name
                 ,l_step
                 ,l_status);

EXCEPTION WHEN OTHERS THEN

    -- Log end process in exception
    l_status := 'KO';
    l_step := 'END';
    INSERT INTO maintenance.process_log(
                 "date"
                 ,execution_id
                 ,"user"
                 ,process
                 ,step
                 ,outcome
                 ,note)
         VALUES (Clock_timestamp()
                 ,l_execution_id
                 ,l_execution_user
                 ,l_process_name
                 ,l_step
                 ,l_status
                 ,Concat('Step: [', l_step,'] , Error: ', SQLERRM));
    COMMIT;
    RAISE WARNING 'An error occurred during archiving data: %', SQLERRM;

END;
$function$ LANGUAGE 'plpgsql'
GO

--changeset liquibase:archive-azureuser-202603090006-03
GRANT EXECUTE
      ON PROCEDURE maintenance.execute_daily_archive_on_table(TEXT, TEXT, BIGINT, BIGINT, BIGINT, DATE, DATE, TEXT, TEXT)
      TO fdr3;
GRANT EXECUTE
      ON PROCEDURE maintenance.execute_daily_archive()
      TO fdr3;