--liquibase formatted sql

-- ## SCHEMAS ##
--changeset liquibase:archive-azureuser-202603090003-01
CREATE SCHEMA IF NOT EXISTS remote_online_db;

-- ## MODULES ##
--changeset liquibase:archive-azureuser-202603090003-02
CREATE SERVER IF NOT EXISTS remote_online_db
       FOREIGN DATA WRAPPER postgres_fdw
       OPTIONS (host '${fdr3-online-host}', port '${fdr3-online-port}', dbname 'fdr3', sslmode 'require');

CREATE USER MAPPING IF NOT EXISTS
       FOR azureuser
       SERVER remote_online_db
       OPTIONS (user 'azureuser', password '${azureuser-online-password}');

--changeset liquibase:archive-azureuser-202603090003-03
CREATE SERVER IF NOT EXISTS remote_postgres
       FOREIGN DATA WRAPPER postgres_fdw
       OPTIONS (host 'localhost', port '${fdr3-online-port}', dbname 'postgres', sslmode 'require');

CREATE USER MAPPING IF NOT EXISTS
       FOR azureuser
       SERVER remote_postgres
       OPTIONS (user 'azureuser', password '${azureuser-online-password}');

-- ## GRANTS ##
--changeset liquibase:archive-azureuser-202603090003-04
GRANT USAGE
      ON FOREIGN SERVER remote_online_db
      TO azureuser;

GRANT USAGE
      ON FOREIGN SERVER remote_online_db
      TO fdr3;

GRANT USAGE
      ON FOREIGN SERVER remote_postgres
      TO azureuser;

-- ## TABLES ##
--changeset liquibase:archive-azureuser-202603090003-05
IMPORT FOREIGN SCHEMA fdr3
       LIMIT TO (flow, payment, mview_flows_published_last_day, mview_payments_published_last_day)
       FROM SERVER remote_online_db
       INTO remote_online_db;

--changeset liquibase:archive-azureuser-202603090003-06
IMPORT FOREIGN SCHEMA cron
       LIMIT TO (job_run_details)
       FROM SERVER remote_postgres
       INTO maintenance;

--changeset liquibase:archive-azureuser-202603090003-07
 ALTER SERVER remote_online_db OPTIONS (ADD fetch_size '50000');