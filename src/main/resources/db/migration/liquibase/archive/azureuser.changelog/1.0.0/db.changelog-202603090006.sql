--liquibase formatted sql

--changeset liquibase:archive-azureuser-202603090006-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.archive_daily(
    IN p_min_id BIGINT DEFAULT NULL,
    IN p_max_id BIGINT DEFAULT NULL
)
AS $function$
DECLARE

    execution_user TEXT := SESSION_USER;
    process_name TEXT := 'archive_daily';
    execution_id TEXT := Gen_random_uuid()::TEXT;

    l_partition_names TEXT[];

    l_step TEXT := 'START';
    l_record RECORD;
    l_stmt TEXT;

    l_min_id BIGINT;
    l_max_id BIGINT;
    l_batch_rows BIGINT;
    l_archived_rows BIGINT := 0;
    
    l_current_start_id BIGINT;
    l_current_end_id BIGINT;

BEGIN

    -- Log start process
    INSERT INTO maintenance.process_log(
                 "date"
                 ,l_execution_id
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
                 ,'OK'
                 ,NULL);
    COMMIT;
    l_step := 'ARCHIVING_DATA';

    FOR l_record IN
           SELECT src_schema_name
                  ,src_table_name
                  ,dst_schema_name
                  ,dst_table_name
                  ,batch_size
                  ,batch_column
                  ,partition_date_column
            FROM maintenance.archive_config
           WHERE is_active IS TRUE
                 AND Lower(archive_type) = 'daily'
        ORDER BY execution_order ASC
    LOOP

        RAISE NOTICE 'Starting archiving data from [%.%] to [%.%]', l_record.src_schema_name, l_record.src_table_name, l_record.dst_schema_name, l_record.dst_table_name;
        l_archived_rows := 0;

        --
        l_stmt := Format('
            SELECT MIN(%I), MAX(%I)
              FROM %I.%I
        ', l_record.batch_column, l_record.batch_column, l_record.src_schema_name, l_record.src_table_name);
        EXECUTE l_stmt INTO l_min_id, l_max_id;

        l_min_id := COALESCE(p_min_id, l_min_id);
        l_max_id := COALESCE(p_max_id, l_max_id);

        IF l_min_id IS NULL OR l_max_id IS NULL
        THEN

            RAISE NOTICE 'No data found to archive for table [%.%]', l_record.src_schema_name, l_record.src_table_name;
            INSERT INTO maintenance.process_log(
                         "date"
                         ,l_execution_id
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
                         ,'OK'
                         ,Concat('Table: [', l_record.src_schema_name, '.', l_record.src_table_name, '], Total records moved: [0]'));
            COMMIT;
            CONTINUE;
        END IF;

        RAISE NOTICE 'Scanning through table [%.%] on column [%] from [%] to [%], with batches of size [%]', l_record.src_schema_name, l_record.src_table_name,
                l_record.batch_column, l_min_id, l_max_id, l_record.batch_size;

        l_current_start_id := l_min_id;
        WHILE l_current_start_id <= l_max_id
        LOOP

            --
            l_current_end_id := l_current_start_id + l_record.batch_size;
            l_stmt := Format('
                SELECT Array_agg(DISTINCT %L || ''_p'' || To_char(Date_trunc(''month'', "%I"), ''YYYYMM''))
                  FROM %I.%I
                 WHERE %I >= $1
                       AND %I < $2
            ', l_record.dst_table_name, l_record.partition_date_column, l_record.src_schema_name, l_record.src_table_name, l_record.batch_column, l_record.batch_column);
            EXECUTE l_stmt INTO l_partition_names USING l_current_start_id, l_current_end_id;

            --
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
            l_archived_rows := l_archived_rows + l_batch_rows;

            --
            l_current_start_id := l_current_end_id;
            UPDATE maintenance.partition_status
               SET "status" = 'U'
                   ,updated_at = Clock_timestamp()
             WHERE schema_name = l_record.dst_schema_name
                   AND table_name = l_record.dst_table_name
                   AND partition_name = ANY(l_partition_names);

            --
            INSERT INTO maintenance.process_log(
                         "date"
                         ,l_execution_id
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
                         ,'OK'
                         ,Concat('Table: [', l_record.src_schema_name, '.', l_record.src_table_name, '], Total records moved: [', l_archived_rows, ']'));
            COMMIT;

        END LOOP;

        IF l_archived_rows > 0
        THEN
            EXECUTE Format('
                ANALYZE %I.%I
            ', l_record.dst_schema_name, l_record.dst_table_name);
            COMMIT;
        END IF;

        INSERT INTO maintenance.process_log(
                     "date"
                     ,l_execution_id
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
                      ,'OK'
                      ,Concat('Table: [', l_record.src_schema_name, '.', l_record.src_table_name, '], Total records moved: [0]'));
        COMMIT;

    END LOOP;

    -- Log end process
    l_step := 'END';
    INSERT INTO maintenance.process_log(
                 "date"
                 ,l_execution_id
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
                 ,'OK'
                 ,NULL);
    COMMIT;

EXCEPTION WHEN OTHERS THEN

    -- Rollback all changes not applied on the current partition
    ROLLBACK;

    -- Log end process in exception
    INSERT INTO maintenance.process_log(
                 "date"
                 ,l_execution_id
                 ,"user"
                 ,process
                 ,step
                 ,outcome
                 ,note)
         VALUES (Clock_timestamp()
                 ,l_execution_id
                 ,l_execution_user
                 ,l_process_name
                 ,'END'
                 ,'KO'
                 ,Concat('Step: ', l_step,' , Error: ', SQLERRM));
    COMMIT;
    RAISE WARNING 'An error occurred during archiving data: %', SQLERRM;

END;
$function$ LANGUAGE 'plpgsql'
GO

--changeset liquibase:archive-azureuser-202603090006-02
GRANT EXECUTE
      ON PROCEDURE maintenance.archive_daily(BIGINT,BIGINT)
      TO fdr3;