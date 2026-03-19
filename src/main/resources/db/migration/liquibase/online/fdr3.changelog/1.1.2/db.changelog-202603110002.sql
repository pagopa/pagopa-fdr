--liquibase formatted sql

--changeset liquibase:fdr3-202603110002-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.delete_unpublished_flows(
    IN p_retention_days INTEGER DEFAULT 1,
    IN p_batch_size BIGINT DEFAULT 1000,
	IN p_start_date TIMESTAMP DEFAULT NULL,
	IN p_end_date TIMESTAMP DEFAULT NULL)
AS $function$
DECLARE

    l_execution_user TEXT := SESSION_USER;
    l_process_name TEXT := 'delete_unpublished_flows';
    l_execution_id TEXT := Gen_random_uuid()::TEXT;

    l_step TEXT := 'START';
    l_status TEXT := 'OK';
    l_operation_process_log_id BIGINT;
    l_end_process_log_id BIGINT;

    l_upper_limit TIMESTAMP;

    l_count BIGINT := -1;
    l_deleted_records BIGINT := 0;
    l_deleted_batches BIGINT := 0;

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
                 ,'START'
                 ,'OK');
    COMMIT;

    -- Log end process (pre-written with incomplete status)
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

    -- Defining upper limit for search
    l_upper_limit := CURRENT_DATE - (p_retention_days || ' days')::INTERVAL;

    IF EXISTS (
        SELECT 1
          FROM fdr3.flow
         WHERE created < l_upper_limit
         LIMIT 1
    )
    THEN

        -- Creating process_log record and using the generated ID in order to update the same record
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
                     ,'CLEANING_DATA'
                     ,'START'
                     ,Concat('Table: [fdr3.flow]'))
          RETURNING id
                    INTO l_operation_process_log_id;
        COMMIT;

        WHILE l_count != 0
        LOOP

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
            l_status := 'OK';
            UPDATE maintenance.process_log
               SET "date" = Clock_timestamp()
                   ,outcome = 'ONGOING'
                   ,note = Concat('Table: [fdr3.flow], Deleted batches: [', l_deleted_batches, '], Deleted records: [', l_deleted_records, ']')
             WHERE id = l_operation_process_log_id;
            COMMIT;

        END LOOP;
        
        -- Update the same process_log record one last time with final info
        UPDATE maintenance.process_log
           SET "date" = Clock_timestamp()
               ,outcome = 'OK'
         WHERE id = l_operation_process_log_id;
        COMMIT;

    ELSE

        RAISE NOTICE 'No data to delete for table [fdr3.flow]!';
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
                     ,'CLEANING_DATA'
                     ,'SKIPPED'
                     ,Concat('Table: [fdr3.flow]'));
        COMMIT;

    END IF;

    -- Update the end process_log record with final info
    UPDATE maintenance.process_log
       SET "date" = Clock_timestamp()
           ,outcome = 'OK'
     WHERE id = l_end_process_log_id;
    COMMIT;

END;
$function$ LANGUAGE 'plpgsql'
GO

--changeset liquibase:fdr3-202603110002-02 endDelimiter:GO
GRANT EXECUTE
   ON PROCEDURE maintenance.delete_unpublished_flows(INTEGER, BIGINT, TIMESTAMP, TIMESTAMP)
   TO azureuser;