--liquibase formatted sql

--changeset liquibase:archive-azureuser-202603090004-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.create_partition_on_next_month()
AS $function$
DECLARE

    l_execution_user TEXT := SESSION_USER;
    l_process_name TEXT := 'create_partition_on_month';
    l_execution_id TEXT := Gen_random_uuid()::TEXT;

    l_partition_from TIMESTAMP;
    l_partition_to TIMESTAMP;

    l_step TEXT := 'START';
    L_RECORD RECORD;

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
                 ,l_execution_id
                 ,l_execution_user
                 ,l_process_name
                 ,l_step
                 ,'OK'
                 ,NULL);
    COMMIT;

    -- Generate partition's date boundaries
    l_partition_from := Date_trunc('month', CURRENT_DATE) + ('1 month')::INTERVAL;
    l_partition_to := Date_trunc('month', CURRENT_DATE) + ('2 month')::INTERVAL;

    FOR l_record IN
        SELECT cfg.schema_name AS schema_name
               ,cfg.table_name AS table_name
               ,Concat(
                   cfg.table_name
                   ,'_p'
                   ,To_char(Date_trunc('month', CURRENT_DATE) + ('1 month')::INTERVAL ,'YYYYMM')
               )               AS partition_name
          FROM maintenance.partition_config cfg
         WHERE cfg.is_active IS TRUE
               AND cfg.retention_type = 'month';
    LOOP

        RAISE NOTICE 'Analyzing new partition for [%.%] table (partition name [%])', l_record.schema_name, l_record.table_name, l_record.partition_name;
        l_step := 'CREATE_PARTITION';

        -- Check for partition on logical catalog
        IF EXISTS (
            SELECT 1
              FROM maintenance.partition_status
             WHERE schema_name = l_record.schema_name
                   AND table_name = l_record.table_name
                   AND partition_name = l_record.partition_name
                   AND "status" = 'I'
        ) THEN

            -- Log skipped partition step
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
                         ,'SKIPPED'
                         ,Concat('Partition already created. Table: ', l_record.schema_name, '.', l_record.table_name, ', Partition: ', l_record.partition_name));

            RAISE NOTICE 'Skipping creation of partition [%] for parent table [%.%] because already in [partition_status] table.', l_record.partition_name,  l_record.schema_name,  l_record.table_name;
            COMMIT;
            CONTINUE;
        END IF;

        -- Check for partition on physical catalog
        IF EXISTS (
            SELECT 1 
              FROM pg_class partition_class
                   JOIN pg_namespace partition_schema 
                     ON partition_schema.oid = partition_class.relnamespace
             WHERE partition_schema.nspname = l_record.schema_name 
                   AND partition_class.relname = l_record.partition_name
        ) THEN

            -- Add partition status on logical catalog, updating it if deleted before
            RAISE WARNING 'Partition [%] for parent table [%.%] exists physically but was not registered in status [partition_status] table. Syncing status...', l_record.partition_name,  l_record.schema_name,  l_record.table_name;
            INSERT INTO maintenance.partition_status (
                         schema_name
                         ,table_name
                         ,partition_name
                         ,"status"
                         ,inserted_at)
                 VALUES (l_record.schema_name
                         ,l_record.table_name
                         ,l_record.partition_name
                         ,'I'
                         ,Clock_timestamp())
            ON CONFLICT (schema_name, table_name, partition_name)
                        DO UPDATE SET "status" = 'I'
                            ,inserted_at = Clock_timestamp()
                            ,updated_at = Clock_timestamp()
                            ,deleted_at = NULL;

            -- Log skipped partition step
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
                         ,'SKIPPED'
                         ,Concat('Partition status aligned with database catalog. Table: ', l_record.schema_name, '.', l_record.table_name, ', Partition: ', l_record.partition_name));
            COMMIT;
            CONTINUE;
        END IF;

        RAISE NOTICE 'Creating new partition [%] for parent table [%.%]', l_record.partition_name, l_record.schema_name, l_record.table_name;

        -- Generate the partition from the master table
        EXECUTE Format('
            CREATE TABLE IF NOT EXISTS %I.%I
            PARTITION OF %I.%I
            FOR VALUES FROM (%L::TIMESTAMP) TO (%L::TIMESTAMP)
        ', l_record.schema_name, l_record.partition_name, l_record.schema_name, l_record.table_name, l_partition_from, l_partition_to);

        -- Generate the record on partition_status, setting status to (I)nserted
        INSERT INTO maintenance.partition_status
                    (schema_name
                     ,table_name
                     ,partition_name
                     ,"status"
                     ,inserted_at)
             VALUES (l_record.schema_name
                     ,l_record.table_name
                     ,l_record.partition_name
                     ,'I'
                     ,Clock_timestamp())

        -- Log generated partition step
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
                     ,Concat('Table: ', l_schema_name, '.', l_table_name, ', Partition: ', l_partition_name));
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
    RAISE WARNING 'An error occurred during create partitions for next month: %', SQLERRM;

END;
$function$ LANGUAGE 'plpgsql'
GO

--changeset liquibase:archive-azureuser-202603090004-02
GRANT EXECUTE
      ON PROCEDURE maintenance.create_partition_on_next_month()
      TO fdr3;