--liquibase formatted sql

-- ## EXTENSIONS ##
--changeset liquibase:admin-archive-202603090003-01
CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- ## SCHEMAS ##
--changeset liquibase:admin-archive-202603090003-02
CREATE SCHEMA remote_fdr3;

-- ## MODULES ##
--changeset liquibase:admin-archive-202603090003-03
CREATE SERVER IF NOT EXISTS remote_fdr3
       FOREIGN DATA WRAPPER postgres_fdw
       OPTIONS (host '${fdr3-online-host}', port '${fdr3-online-port}', dbname 'fdr3', sslmode 'require');

CREATE USER MAPPING IF NOT EXISTS
       FOR azureuser
       SERVER remote_fdr3
       OPTIONS (user 'azureuser', password '${fdr3-online-password}');

-- ## GRANTS ##
--changeset liquibase:admin-archive-202603090003-04
GRANT USAGE
      ON FOREIGN SERVER remote_fdr3
      TO azureuser;

GRANT USAGE
      ON FOREIGN SERVER remote_fdr3
      TO fdr3;

-- ## TABLES ##
--changeset liquibase:admin-archive-202603090003-05
IMPORT FOREIGN SCHEMA fdr3
       LIMIT TO (mview_flows_published_last_day, mview_payments_published_last_day)
       FROM SERVER remote_fdr3
       INTO remote_fdr3;