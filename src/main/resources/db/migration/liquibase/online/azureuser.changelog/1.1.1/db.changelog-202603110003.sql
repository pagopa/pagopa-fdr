--liquibase formatted sql

--changeset liquibase:azureuser-202603110003-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.execute_data_cleansing()
AS $function$
DECLARE

    l_execution_user TEXT := SESSION_USER;
    l_process_name TEXT := 'execute_data_cleansing';
    l_execution_id TEXT := Gen_random_uuid()::TEXT;

    l_operation_process_log_id BIGINT;
    l_end_process_log_id BIGINT;

    l_step TEXT := 'START';
    l_status TEXT := 'OK';
    l_record RECORD;
    l_boundaries RECORD;
    l_stmt TEXT;
    l_logged_stmts TEXT;

    l_min_id BIGINT;
    l_max_id BIGINT;
    l_batch_rows BIGINT;
    l_cleaned_rows BIGINT := 0;

    l_current_start_id BIGINT;
    l_current_end_id BIGINT;

    l_is_failed BOOLEAN := false;
    l_has_error BOOLEAN;
    l_error_msg TEXT;

BEGIN

    -- Log start process
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
    COMMIT;

    -- Log end process, pre-written with incomplete status
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
                 ,'END'
                 ,'KO')
      RETURNING id
                INTO l_end_process_log_id;
    COMMIT;


    -- Retrieve retention configuration for required table
    FOR l_record IN
        SELECT cfg.schema_name
               ,cfg.table_name
               ,cfg.retention_type
               ,cfg.retention
               ,cfg.batch_column
               ,cfg.batch_size
               ,cfg.retention_date_column
          FROM maintenance.retention_config cfg
         WHERE cfg.is_active IS TRUE
         ORDER BY cfg.execution_order ASC
    LOOP

        BEGIN
            -- Creating process_log record and using the generated ID in order to update the same record
            l_step := 'PRUNING_DATA';
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
                          ,Concat('Table [', l_record.schema_name, '.', l_record.table_name, '], Rows: [', l_cleaned_rows, ']'))
               RETURNING id
                         INTO l_operation_process_log_id;

            RAISE NOTICE 'Analyzing [%.%] table for data cleansing', l_record.schema_name, l_record.table_name;

            l_status := 'OK';
            l_has_error := false;
            l_error_msg := NULL;
            l_stmt := NULL;
            l_logged_stmts := NULL;

            -- Check for partition on physical catalog
            IF NOT EXISTS (
                SELECT 1
                  FROM pg_class rel
                       JOIN pg_namespace rel_schema
                         ON rel.relnamespace = rel_schema.oid
                 WHERE Lower(l_record.schema_name) = Lower(rel_schema.nspname)
                       AND Lower(l_record.table_name) = Lower(rel.relname)
            ) THEN

                -- Log skipped data cleansing step on non-existing table
                RAISE NOTICE 'Skipping cleansing for table [%.%] because not existent in [pg_class] table.',  l_record.schema_name,  l_record.table_name;
                UPDATE maintenance.process_log
                   SET "date" = Clock_timestamp()
                       ,outcome = 'SKIPPED'
                       ,note = Concat('No table [', l_record.schema_name, '.', l_record.table_name, '] found in physical catalog.')
                 WHERE id = l_operation_process_log_id;

            ELSE

                -- Build dynamic SQL for min/max boundaries
                l_stmt := Format('
                    SELECT Min(%I) AS min_id, Max(%I) AS max_id
                    FROM %I.%I
                    WHERE %I < Now()::DATE - (%L * interval ''1 %s'')
                ', l_record.batch_column, l_record.batch_column, l_record.schema_name, l_record.table_name
                 , l_record.retention_date_column, l_record.retention, l_record.retention_type);
                 l_logged_stmts := regexp_replace(l_stmt, '\s+', ' ', 'g');

                -- Use FOR ... IN EXECUTE to fetch into variables
                l_min_id := NULL;
                l_max_id := NULL;
                FOR l_boundaries IN EXECUTE l_stmt
                LOOP
                    l_min_id := l_boundaries.min_id;
                    l_max_id := l_boundaries.max_id;
                END LOOP;

                IF l_min_id IS NULL OR l_max_id IS NULL
                THEN
                    -- Update the same process_log record with updated info, setting date with current timestamp
                    RAISE NOTICE 'No data to clean found for table [%.%]', l_record.schema_name, l_record.table_name;
                    UPDATE maintenance.process_log
                       SET "date" = Clock_timestamp()
                           ,outcome = 'SKIPPED'
                           ,note = Concat('No data found in table [', l_record.schema_name, '.', l_record.table_name, '].')
                           ,statement = l_logged_stmts
                     WHERE id = l_operation_process_log_id;

                ELSE
                    RAISE NOTICE 'Analyzing [%.%] table for data cleansing in bulk [%s - %s]', l_record.schema_name, l_record.table_name, l_min_id, l_max_id;
                    l_current_start_id := l_min_id;
                    WHILE l_current_start_id <= l_max_id
                    LOOP

                        -- Delete records in batch
                        l_current_end_id := l_current_start_id + l_record.batch_size;
        				RAISE NOTICE 'No data to clean found for table [%.%]', l_record.schema_name, l_record.table_name;
                        l_stmt := Format('
                            DELETE FROM %I.%I
                             WHERE %I >= $1
                                   AND %I < $2
                        ', l_record.schema_name, l_record.table_name, l_record.batch_column, l_record.batch_column);
                        EXECUTE l_stmt USING l_current_start_id, l_current_end_id;

                        -- Calculating statistics
                        GET DIAGNOSTICS l_batch_rows = ROW_COUNT;
                        l_cleaned_rows := l_cleaned_rows + l_batch_rows;

                        -- Update the same process_log record with updated info, setting date with current timestamp
                        l_logged_stmts := l_logged_stmts || ' ||| ' || Concat(regexp_replace(l_stmt, '\s+', ' ', 'g'), ', $1: [', l_current_start_id, '], $2: [', l_current_end_id, ']');
                        UPDATE maintenance.process_log
                           SET "date" = Clock_timestamp()
                               ,outcome = l_status
                               ,note = Concat('Table [', l_record.schema_name, '.', l_record.table_name, '], Rows: [', l_cleaned_rows, ']')
                               ,statement = l_logged_stmts
                         WHERE id = l_operation_process_log_id;

                        l_current_start_id := l_current_end_id;
                    END LOOP;

                    IF l_cleaned_rows > 0 THEN
                        EXECUTE Format('ANALYZE %I.%I', l_record.schema_name, l_record.table_name);
                    END IF;

                END IF;
            END IF;

        EXCEPTION WHEN OTHERS THEN
            l_status := 'KO';
            l_is_failed := true;
            l_has_error := true;
            l_error_msg := SQLERRM;
        END;

        IF l_has_error THEN

            RAISE WARNING 'Error on data cleansing operation for [%.%] table: %', l_record.schema_name, l_record.table_name, l_error_msg;
            l_logged_stmts := l_logged_stmts || ' ||| ' || Concat(regexp_replace(l_stmt, '\s+', ' ', 'g'), ', $1: [', l_current_start_id, '], $2: [', l_current_end_id, ']');
            INSERT INTO maintenance.process_log(
                            "date"
                            ,execution_id
                            ,"user"
                            ,process
                            ,step
                            ,outcome
                            ,note
                            ,statement)
                VALUES (Clock_timestamp()
                        ,l_execution_id
                        ,l_execution_user
                        ,l_process_name
                        ,l_step
                        ,l_status
                        ,Concat('Table: ', l_record.schema_name, '.', l_record.table_name, ', Step: ', l_step,' , Error: ', l_error_msg)
                        ,l_logged_stmts);
        END IF;
        COMMIT;

    END LOOP;

    -- Log end process
    IF l_is_failed THEN
        l_status := 'KO';
    ELSE
        l_status := 'OK';
    END IF;
    l_step := 'END';
	UPDATE maintenance.process_log
       SET "date" = Clock_timestamp()
           ,step = l_step
           ,outcome = l_status
     WHERE id = l_end_process_log_id;
    COMMIT;

END;
$function$ LANGUAGE 'plpgsql'
GO

--changeset liquibase:archive-azureuser-202603110003-02
GRANT EXECUTE
      ON PROCEDURE maintenance.execute_data_cleansing()
      TO fdr3;