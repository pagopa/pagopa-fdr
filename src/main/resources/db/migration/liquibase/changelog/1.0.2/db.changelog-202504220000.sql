--liquibase formatted sql

-- ## INDEXES ##
--changeset liquibase:202504220000-01
CREATE INDEX payment_by_iuv_idx ON fdr3.payment (iuv);
--changeset liquibase:202504220000-02
CREATE INDEX payment_by_iur_idx ON fdr3.payment (iur);