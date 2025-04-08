--liquibase formatted sql

-- ## SEQUENCES ##
--changeset liquibase:1738839600000-01
ALTER TABLE fdr3.flow ALTER COLUMN tot_amount TYPE float8 USING tot_amount::float8;
--changeset liquibase:1738839600000-02
ALTER TABLE fdr3.flow ALTER COLUMN computed_tot_amount TYPE float8 USING computed_tot_amount::float8;
--changeset liquibase:1738839600000-03
ALTER TABLE fdr3.payment ALTER COLUMN amount TYPE float8 USING amount::float8;