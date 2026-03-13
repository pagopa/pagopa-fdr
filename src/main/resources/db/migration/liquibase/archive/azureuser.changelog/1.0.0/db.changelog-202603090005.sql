--liquibase formatted sql

--changeset liquibase:archive-azureuser-202603090005-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.delete_partition(
    IN  p_schema_name TEXT,
    IN  p_table_name TEXT,
    IN  p_partition_name TEXT,
    IN  p_execution_user TEXT DEFAULT NULL,
    IN  p_execution_id TEXT DEFAULT NULL
)
AS $function$
DECLARE

    l_execution_user TEXT;
    l_process_name TEXT := 'delete_partition';
    l_execution_id TEXT;

    l_delete_partition_stmt TEXT;
    l_step TEXT := 'START';

BEGIN

    -- Checking required parameters
    IF p_schema_name IS NULL
       OR p_table_name IS NULL
       OR p_partition_name IS NULL
    THEN
        RAISE EXCEPTION 'p_schema_name, p_table_name and p_partition_name must not be NULL';
    END IF;

    -- Setting execution user
    l_execution_user := p_execution_user;
    IF l_execution_user IS NULL
    THEN
        l_execution_user := SESSION_USER;
    END IF;

    -- Setting execution trace ID
    l_execution_id := p_execution_id;
    IF l_execution_id IS NULL
    THEN
        l_execution_id := Gen_random_uuid()::TEXT;
    END IF;

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

    IF EXISTS (
        SELECT 1
          FROM maintenance.partition_status
         WHERE schema_name = p_schema_name
               AND table_name = p_table_name
               AND partition_name = p_partition_name
               AND "status" != 'D'
    )
    THEN

        RAISE NOTICE 'Deleting partition [%] from [%.%] parent table', p_partition_name, p_schema_name, p_table_name;
        l_step := 'DELETE_PARTITION';

        -- Delete the partition from the database
        l_delete_partition_stmt := Format('
            DROP TABLE IF EXISTS %I.%I
        ', p_schema_name, p_partition_name);
        EXECUTE l_delete_partition_stmt;

        -- Update the record on partition_status setting status to (D)eleted
        UPDATE maintenance.partition_status
           SET "status" = 'D'
               ,deleted_at = Clock_timestamp()
         WHERE schema_name = p_schema_name
               AND table_name = p_table_name
               AND partition_name = p_partition_name;

        -- Log deleted partition step
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
                     ,CONCAT('Table: [', p_schema_name, '.', p_table_name, '], Partition: [', p_partition_name, ']'));

    ELSE
        RAISE WARNING 'No valid partition [%] found for parent table [%.%]', p_partition_name, p_schema_name, p_table_name;
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
                     ,Concat(
                         'Partition not found or already deleted. Parent table: [',
                         p_schema_name,
                         '.',
                         p_table_name,
                         '], Partition: [',
                         p_partition_name,
                         ']'
                     ));
    END IF;

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
                 ,CONCAT('Step: ', l_step,' , Error: ', SQLERRM));
    RAISE;

END;
$function$ LANGUAGE 'plpgsql'
SECURITY DEFINER
         SET search_path = fdr3, pg_temp;
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

    partition_cfg_cursor CURSOR FOR
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
                     Substr(child.relname, Length(child.relname)-5, 6)::INT
                     <
                     To_char(
                         Date_trunc('month', CURRENT_DATE) - (cfg.retention || ' month')::INT,
                         'YYYYMM'
                     )::INT
                   );

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

    OPEN partition_cfg_cursor;
    LOOP

        -- Retrieve partition configuration from cursor
        FETCH NEXT
              FROM partition_cfg_cursor
              INTO l_schema_name
                   ,l_table_name
                   ,l_partition_name;
        EXIT WHEN NOT FOUND;

        RAISE NOTICE 'Analyzing expired partition to delete for [%s.%s] table (partition name [%s])', l_schema_name, l_table_name, l_partition_name;
        CALL maintenance.delete_partition(
            p_schema_name => l_schema_name,
            p_table_name => l_table_name,
            p_partition_name => l_partition_name,
            p_execution_user => l_execution_user,
            p_execution_id => l_execution_id
        );

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
                 ,CONCAT('Step: ', l_step,' , Error: ', SQLERRM));

END;
$function$ LANGUAGE 'plpgsql'
SECURITY DEFINER
         SET search_path = fdr3, pg_temp;
GO


--changeset liquibase:archive-azureuser-202603090005-03
GRANT EXECUTE
      ON PROCEDURE maintenance.delete_partition(TEXT,TEXT,TEXT,TEXT,TEXT)
      TO fdr3;
GRANT EXECUTE
      ON PROCEDURE maintenance.delete_expired_partitions()
      TO fdr3;