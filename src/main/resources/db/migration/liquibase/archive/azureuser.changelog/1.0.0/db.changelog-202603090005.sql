--liquibase formatted sql

--changeset liquibase:archive-azureuser-202603090005-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.delete_partition(
    IN p_schema_name TEXT,
    IN p_table_name TEXT,
    IN p_partition_name TEXT,
    IN p_execution_user TEXT DEFAULT NULL,
    IN p_execution_id TEXT DEFAULT NULL
)
AS $function$
DECLARE

    l_execution_user TEXT := COALESCE(p_execution_user, SESSION_USER);
    l_process_name TEXT := 'delete_partition';
    l_execution_id TEXT := COALESCE(p_execution_id, Gen_random_uuid()::TEXT);

    l_operation_process_log_id BIGINT;
    l_end_process_log_id BIGINT;

    l_step TEXT := 'START';
    l_status TEXT := 'OK';

    l_is_failed BOOLEAN := false;
    l_has_error BOOLEAN;
    l_error_msg TEXT;

BEGIN
    -- Log start process
    INSERT INTO maintenance.process_log(
                 "date"
                 ,l_execution_id
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

    -- Checking required parameters
    IF p_schema_name IS NULL
       OR p_table_name IS NULL
       OR p_partition_name IS NULL
    THEN

        -- Update the end process_log record with error
        UPDATE maintenance.process_log
           SET "date" = Clock_timestamp()
               ,step = l_step
               ,outcome = l_status
               ,note = 'p_schema_name, p_table_name and p_partition_name cannot be NULL'
         WHERE id = l_end_process_log_id;
        COMMIT;

        RAISE EXCEPTION 'p_schema_name, p_table_name and p_partition_name cannot be NULL';

    END IF;

    -- Log deleted partition step, pre-written with incomplete status
    l_step := 'DELETE_PARTITION';
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
                 ,'ONGOING'
                 ,Concat('Table: [', p_schema_name, '.', p_table_name, '], Partition: [', p_partition_name, ']'))
       RETURNING id
                 INTO l_operation_process_log_id;
    COMMIT;

    -- Check for partition on logical catalog
    IF EXISTS (
        SELECT 1
          FROM maintenance.partition_status
         WHERE schema_name = p_schema_name
               AND table_name = p_table_name
               AND partition_name = p_partition_name
               AND "status" != 'D'
    )
    THEN
        -- Starting a sub-transaction in order to generate process_log record about the error
        BEGIN

            -- Detach and delete the partition from the database
            RAISE NOTICE 'Deleting partition [%] from [%.%] parent table', p_partition_name, p_schema_name, p_table_name;
            EXECUTE Format(
                        'ALTER TABLE %I.%I DETACH PARTITION %I.%I CONCURRENTLY'
                        ,p_schema_name
                        ,p_table_name
                        ,p_schema_name
                        ,p_partition_name);
            EXECUTE Format(
                        'DROP TABLE IF EXISTS %I.%I'
                        ,p_schema_name
                        ,p_partition_name);

            -- Update the record on partition_status setting status to (D)eleted
            UPDATE maintenance.partition_status
               SET "status" = 'D'
                   ,deleted_at = Clock_timestamp()
             WHERE schema_name = p_schema_name
                   AND table_name = p_table_name
                   AND partition_name = p_partition_name;

            -- Update the end process_log record with error
            UPDATE maintenance.process_log
               SET "date" = Clock_timestamp()
                   ,step = l_step
                   ,outcome = l_status
             WHERE id = l_end_process_log_id;
            COMMIT;

        -- Catch SQLERRM and separately handle errors (in order to commit process_log record)
        EXCEPTION WHEN OTHERS THEN
            l_status := 'KO';
            l_is_failed := true;
            l_has_error := true;
            l_error_msg := SQLERRM;
        END;

        -- Handle errors if an exception is found
        IF l_has_error THEN

            -- Update the operation process_log record with error
            UPDATE maintenance.process_log
               SET "date" = Clock_timestamp()
                   ,outcome = l_status
                   ,note = Concat('Table: [', l_record.schema_name, '.', l_record.table_name,
                               '], Partition: [', l_record.partition_name,
                               '], Step: [', l_step,
                               '], Error: ', l_error_msg));
             WHERE id = l_operation_process_log_id;
             RAISE WARNING 'An error occurred during delete partition [%] for parent table [%.%]: %', p_partition_name, p_schema_name, p_table_name, l_error_msg;
        END IF;

        COMMIT;

    ELSE
        -- Update the operation process_log record with error
        RAISE WARNING 'No valid partition [%] found for parent table [%.%]', p_partition_name, p_schema_name, p_table_name;
        UPDATE maintenance.process_log
           SET "date" = Clock_timestamp()
               ,outcome = 'SKIPPED'
               ,note = Concat('Partition not found or already deleted. Parent table: [', p_schema_name, '.', p_table_name, '], Partition: [', p_partition_name, ']'));
          WHERE id = l_end_process_log_id;
        COMMIT;
    END IF;

    -- Update the end process_log record with final info
    IF l_is_failed = true THEN
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


--changeset liquibase:archive-azureuser-202603090005-02 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.delete_expired_partitions()
AS $function$
DECLARE

    l_execution_user TEXT := SESSION_USER;
    l_process_name TEXT := 'delete_expired_partitions';
    l_execution_id TEXT := Gen_random_uuid()::TEXT;

    l_schema_name TEXT;
    l_table_name TEXT;
    l_partition_name TEXT;

    l_step TEXT := 'START';
    l_record RECORD;

BEGIN

    -- Log start process
    INSERT INTO maintenance.process_log(
                 "date"
                 ,l_execution_id
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

    FOR l_record IN
        SELECT child_schema.nspname  AS schema_name
               ,cfg.table_name       AS table_name
               ,child.relname        AS partition_name
          FROM pg_inherits
               JOIN pg_class child
                 ON pg_inherits.inhrelid = child.oid
               JOIN pg_namespace child_schema
                 ON child.relnamespace = child_schema.oid
               JOIN pg_class parent
                 ON pg_inherits.inhparent = parent.oid
               JOIN pg_namespace parent_schema
                 ON parent.relnamespace = parent_schema.oid
               JOIN maintenance.partition_config cfg
                 ON Lower(cfg.schema_name) = Lower(parent_schema.nspname)
                AND Lower(cfg.table_name) = Lower(parent.relname)
         WHERE child.relname ~ '_p[0-9]{6}$'
               AND cfg.status IS TRUE
               AND Lower(cfg.retention_type) = 'month'
               AND (
                     To_date(Substring(child.relname FROM Length(child.relname)-5 FOR 6), 'YYYYMM')
                     <
                     Date_trunc('month', CURRENT_DATE) - (cfg.retention || ' months')::INTERVAL
                   )
    LOOP
        -- Starting a sub-transaction in order to generate process_log record about the error
        BEGIN

            RAISE NOTICE 'Analyzing expired partition [%] to delete for [%s.%s] table', l_record.partition_name, l_record.schema_name, l_record.table_name;
            CALL maintenance.delete_partition(
                p_schema_name => l_record.schema_name,
                p_table_name => l_record.table_name,
                p_partition_name => l_record.partition_name,
                p_execution_user => l_execution_user,
                p_execution_id => l_execution_id
            );

        -- Catch SQLERRM and separately handle errors (in order to commit process_log record)
        EXCEPTION WHEN OTHERS THEN
            l_status := 'KO';
            l_is_failed := true;
            l_has_error := true;
            l_error_msg := SQLERRM;
        END;

        -- Handle errors if an exception is found
        IF l_has_error THEN

            -- Update the operation process_log record with error
            UPDATE maintenance.process_log
               SET "date" = Clock_timestamp()
                   ,outcome = l_status
                   ,note = Concat('Step: [', l_step, '], Error: ', l_error_msg));
             WHERE id = l_end_process_log_id;
            RAISE WARNING 'An error occurred during delete expired partitions: %', SQLERRM;

        END IF;

    END LOOP;

    -- Update the end process_log record with final info
    IF l_is_failed = true THEN
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


--changeset liquibase:archive-azureuser-202603090005-03
GRANT EXECUTE
      ON PROCEDURE maintenance.delete_partition(TEXT,TEXT,TEXT,TEXT,TEXT)
      TO fdr3;
GRANT EXECUTE
      ON PROCEDURE maintenance.delete_expired_partitions()
      TO fdr3;