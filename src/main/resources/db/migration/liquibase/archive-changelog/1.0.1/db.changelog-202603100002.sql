--liquibase formatted sql

--changeset liquibase:202603090004-01 endDelimiter:GO
CREATE OR REPLACE PROCEDURE maintenance.delete_partitions()
AS $function$
DECLARE

    execution_user TEXT := USER;
    process_name TEXT := 'delete_partition';
    execution_id TEXT := Gen_random_uuid()::TEXT;

    l_schema_name TEXT;
    l_table_name TEXT;
    l_partition_name TEXT;

    l_delete_partition_stmt TEXT;
    l_step TEXT := "START"

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
                 ON child.relnamespace = parent_schema.oid
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
                   )

BEGIN

    -- Log start process
    INSERT INTO maintenance.process_log
         VALUES (Clock_timestamp()
                 ,execution_id
                 ,execution_user
                 ,process_name
                 ,l_step
                 ,"OK"
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

        RAISE NOTICE "Analyzing expired partition to delete for [%s.%s] table (partition name [%s])", l_schema_name, l_table_name, l_partition_name;

        -- Check if the analyzed partition was already deleted by past process
        IF EXISTS (
            SELECT *
            FROM maintenance.partition_status
            WHERE schema_name = l_schema_name
                  AND table_name = l_table_name
                  AND partition_name = l_partition_name
                  AND "status" != 'D'
        )
        THEN

            RAISE NOTICE "Deleting expired partition for [%s.%s] table (partition name [%s])", l_schema_name, l_table_name, l_partition_name;
            l_step := "DELETE_PARTITION"

            -- Delete the partition from the database
            l_delete_partition_stmt := Format('
                DROP TABLE %I.%I
            ', l_schema_name, l_partition_name)
            EXECUTE l_delete_partition_stmt;

            -- Update the record on partition_status setting status to (D)eleted
            UPDATE maintenance.partition_status
               SET "status" = 'D'
                   ,deleted_at = Clock_timestamp()
             WHERE schema_name = l_schema_name
                   AND table_name = l_table_name
                   AND partition_name = l_partition_name

            -- Log deleted partition step
            INSERT INTO maintenance.process_log
                 VALUES (Clock_timestamp()
                         ,execution_id
                         ,execution_user
                         ,process_name
                         ,l_step
                         ,"OK"
                         ,CONCAT('Table: ', l_schema_name, '.', l_table_name, ', Partition: ', l_partition_name));

        END IF;

    END LOOP;
    CLOSE partition_cfg_cursor;

    -- Log end process
    l_step := "END"
    INSERT INTO maintenance.process_log
         VALUES (Clock_timestamp()
                 ,execution_id
                 ,execution_user
                 ,process_name
                 ,l_step
                 ,"OK"
                 ,NULL);

EXCEPTION WHEN OTHERS THEN

    -- Log end process in exception
    INSERT INTO maintenance.process_log
         VALUES (Clock_timestamp()
                 ,execution_id
                 ,execution_user
                 ,process_name
                 ,"END"
                 ,"KO"
                 ,CONCAT('Step: ', l_step,' , Error: ',sqlerrm));

END;
$function$ LANGUAGE 'plpgsql'
SECURITY DEFINER
SET search_path = fdr3, pg_temp;
GO

GRANT EXECUTE
      ON PROCEDURE fdr3.delete_partitions()
      TO azureuser;