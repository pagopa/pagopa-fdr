--liquibase formatted sql

--changeset liquibase:admin-archive-202603090004-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.create_partition_on_next_month()
AS $function$
DECLARE

    l_execution_user TEXT := SESSION_USER;
    l_process_name TEXT := 'create_partition_on_month';
    l_execution_id TEXT := Gen_random_uuid()::TEXT;

    l_schema_name TEXT;
    l_table_name TEXT;
    l_partition_name TEXT;

    l_create_partition_stmt TEXT;
    l_partition_from TIMESTAMP;
    l_partition_to TIMESTAMP;

    l_step TEXT := 'START';

    partition_cfg_cursor CURSOR FOR
        SELECT cfg.schema_name AS schema_name
               ,cfg.table_name AS table_name
               ,Concat(
                   cfg.table_name,
                   '_p',
                   To_char(
                       Date_trunc('month', CURRENT_DATE) + ('1 month')::INTERVAL
                       ,'YYYYMM'
                   )
               )               AS partition_name
          FROM maintenance.partition_config cfg
         WHERE cfg.is_active IS TRUE
               AND cfg.retention_type = 'month';

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

    -- Generate partition's date boundaries
    l_partition_from := Date_trunc('month', CURRENT_DATE) + ('1 month')::INTERVAL;
    l_partition_to := Date_trunc('month', CURRENT_DATE) + ('2 month')::INTERVAL;

    OPEN partition_cfg_cursor;
    LOOP

        -- Retrieve partition configuration from cursor
        FETCH NEXT
              FROM partition_cfg_cursor
              INTO l_schema_name
                   ,l_table_name
                   ,l_partition_name;
        EXIT WHEN NOT FOUND;

        RAISE NOTICE 'Analyzing new partition for [%.%] table (partition name [%])', l_schema_name, l_table_name, l_partition_name;

        IF NOT EXISTS (
            SELECT 1
              FROM maintenance.partition_status
             WHERE schema_name = l_schema_name
                   AND table_name = l_table_name
                   AND partition_name = l_partition_name
        ) THEN

            RAISE NOTICE 'Creating new partition for [%.%] table (partition name [%])', l_schema_name, l_table_name, l_partition_name;
            l_step := 'CREATE_PARTITION';

            -- Generate the partition from the master table
            l_create_partition_stmt := Format('
                CREATE TABLE IF NOT EXISTS %I.%I
                PARTITION OF %I.%I
                FOR VALUES FROM (%L) TO (%L)
            ', l_schema_name, l_partition_name, l_schema_name, l_table_name, l_partition_from, l_partition_to)
            EXECUTE l_create_partition_stmt;

            -- Generate the record on partition_status, setting status to (I)nserted
            INSERT INTO maintenance.partition_status
                        (schema_name
                         ,table_name
                         ,partition_name
                         ,"status"
                         ,inserted_at)
                 VALUES (l_schema_name
                         ,l_table_name
                         ,l_partition_name, 'I', clock_timestamp());

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

        END IF;

    END LOOP;
    CLOSE partition_cfg_cursor;

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

EXCEPTION WHEN OTHERS THEN

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

END;
$function$ LANGUAGE 'plpgsql'
SECURITY DEFINER
         SET search_path = fdr3, pg_temp;
GO

--changeset liquibase:admin-archive-202603090004-02
GRANT EXECUTE
      ON PROCEDURE maintenance.create_partition_on_next_month()
      TO fdr3;