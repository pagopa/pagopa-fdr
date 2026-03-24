--liquibase formatted sql

-- ## MODULES ##
--changeset liquibase:azureuser-202603110002-01
CREATE SERVER IF NOT EXISTS remote_postgres
       FOREIGN DATA WRAPPER postgres_fdw
       OPTIONS (host 'localhost', port '${fdr3-online-port}', dbname 'postgres', sslmode 'require');

CREATE USER MAPPING IF NOT EXISTS
       FOR azureuser
       SERVER remote_postgres
       OPTIONS (user 'azureuser', password '${azureuser-online-password}');

-- ## GRANTS ##
--changeset liquibase:azureuser-202603110002-02
GRANT USAGE
      ON FOREIGN SERVER remote_postgres
      TO azureuser;

-- ## TABLES ##
--changeset liquibase:azureuser-202603110002-03
IMPORT FOREIGN SCHEMA cron
       LIMIT TO (job_run_details)
       FROM SERVER remote_postgres
       INTO maintenance;