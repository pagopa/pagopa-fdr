--liquibase formatted sql

-- ## INDEXES ##
--changeset liquibase:202510060000-01
CREATE INDEX IF NOT EXISTS orgid_by_status_latest_idx
    ON fdr3.flow USING btree
    (org_domain_id COLLATE pg_catalog."default" ASC NULLS LAST, status COLLATE pg_catalog."default" ASC NULLS LAST, is_latest ASC NULLS LAST, date DESC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;
--changeset liquibase:202510060000-02
CREATE INDEX IF NOT EXISTS flow_date_idx
    ON fdr3.flow USING btree
    (date ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;
--changeset liquibase:202510060000-03
CREATE INDEX IF NOT EXISTS flow_name_idx
    ON fdr3.flow USING btree
    (name COLLATE pg_catalog."default" DESC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;