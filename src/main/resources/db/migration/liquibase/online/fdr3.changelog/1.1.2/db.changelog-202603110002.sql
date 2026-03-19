--liquibase formatted sql

--changeset liquibase:fdr3-202603110002-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.delete_unpublished_flows(
    IN p_retention_days INTEGER DEFAULT 1,
    IN p_batch_size BIGINT DEFAULT 1000,
	IN p_start_date TIMESTAMP DEFAULT NULL,
	IN p_end_date TIMESTAMP DEFAULT NULL)
DECLARE

    l_execution_user TEXT := COALESCE(p_execution_user, SESSION_USER);
    l_process_name TEXT := 'delete_unpublished_flows';
    l_execution_id TEXT := COALESCE(p_execution_id, Gen_random_uuid()::TEXT);

    l_step TEXT := 'START';
    l_status TEXT := 'OK';

    l_stmt TEXT;
    l_min_id BIGINT;
    l_max_id BIGINT;
    l_upper_limit BIGINT;

    l_count BIGINT := -1;
    l_deleted_records BIGINT := 0;
    l_deleted_batches BIGINT := 0;

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
    l_step := 'CLEANING_DATA';

    -- Defining upper limit for search
    l_upper_limit := CURRENT_DATE - ('INTERVAL \'' || p_retention_days || ' day \'')

    IF EXISTS (
        SELECT 1
          FROM fdr3.flow
         WHERE created < l_upper_limit
         LIMIT 1
    )
    THEN

        -- Creating process_log record and using the generated ID in order to update the same record
        BEGIN
            l_status := 'START';
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
                         ,Concat('Table: [fdr3.flow]'))
              RETURNING id
                        INTO l_process_log_id;
        END;

        l_status := 'ONGOING';
        WHILE l_count > 0 LOOP

            -- Deleting records from table in sized batch
            RAISE NOTICE 'Cleaning batch [%] with size [%]...', l_deleted_batches, p_batch_size;
            DELETE FROM fdr3.flow
             WHERE id IN (
                SELECT id
                FROM fdr3.flow
                WHERE created < l_upper_limit
                LIMIT p_batch_size
            );

            -- Calculating statistics
            GET DIAGNOSTICS l_count = ROW_COUNT;
            l_deleted_records := l_deleted_records + l_count;
            l_deleted_batches := l_deleted_batches + 1;

            -- Update the same process_log record with updated info, setting date with current timestamp
            BEGIN
                l_status := 'OK';
                UPDATE maintenance.process_log
                   SET "date" = Clock_timestamp()
                       ,outcome = l_status,
                       ,note = Concat('Table: [fdr3.flow], Deleted batches: [', l_deleted_batches, '], Deleted records: [', l_deleted_records, ']')
                 WHERE id = l_process_log_id;
            END;
    
        END LOOP;
        
        -- Update the same process_log record one last time with final info
        l_status := 'OK';
        UPDATE maintenance.process_log
           SET "date" = Clock_timestamp()
               ,outcome = l_status)
         WHERE id = l_process_log_id;

    ELSE

        RAISE NOTICE 'No data to delete for table [fdr3.flow]!';
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
                     ,Concat('Table: [fdr3.flow]'));

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
                      ,l_execution_id
                      ,l_execution_user
                      ,l_process_name
                      ,l_step
                      ,l_status);
     END;

EXCEPTION WHEN OTHERS THEN

    -- Update the same process_log record one last time with error info
    l_status := 'KO';
    l_step := 'CLEANING_DATA';
    UPDATE maintenance.process_log
       SET "date" = Clock_timestamp()
           ,step = l_step
           ,outcome = l_status
           ,note = Concat('Table: [fdr3.flow], Deleted batches: [', l_deleted_batches,
               '], Deleted records: [', l_deleted_records,
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
                 ,l_execution_id
                 ,l_execution_user
                 ,l_process_name
                 ,l_step
                 ,l_status);

    RAISE WARNING 'Error on cleaning operation for [fdr3.flow] table: %', l_error_msg;

END;
$function$ LANGUAGE 'plpgsql'
GO