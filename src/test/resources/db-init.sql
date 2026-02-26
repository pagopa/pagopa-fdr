CREATE SEQUENCE IF NOT EXISTS flow_sequence START WITH 1 INCREMENT BY 1;
-- CREATE SEQUENCE IF NOT EXISTS payment_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS flow_to_history_sequence START WITH 1 INCREMENT BY 1;


CREATE TABLE flow (
                      id bigint NOT NULL DEFAULT nextval('flow_sequence'::regclass),
                      name character varying(255) COLLATE pg_catalog."default" NOT NULL,
                      date timestamp(6) without time zone NOT NULL,
                      revision bigint NOT NULL,
                      status character varying(50) COLLATE pg_catalog."default" NOT NULL,
                      is_latest boolean NOT NULL DEFAULT false,
                      psp_domain_id character varying(50) COLLATE pg_catalog."default" NOT NULL,
                      org_domain_id character varying(50) COLLATE pg_catalog."default" NOT NULL,
                      tot_payments bigint NOT NULL DEFAULT 0,
                      tot_amount numeric(19,2) NOT NULL DEFAULT 0,
                      computed_tot_payments bigint NOT NULL DEFAULT 0,
                      computed_tot_amount numeric(19,2) NOT NULL DEFAULT 0,
                      regulation character varying(255) COLLATE pg_catalog."default" NOT NULL,
                      regulation_date timestamp(6) without time zone NOT NULL,
                      sender_id character varying(50) COLLATE pg_catalog."default" NOT NULL,
                      sender_type character varying(50) COLLATE pg_catalog."default" NOT NULL,
                      sender_psp_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
                      sender_psp_broker_id character varying(50) COLLATE pg_catalog."default" NOT NULL,
                      sender_channel_id character varying(50) COLLATE pg_catalog."default" NOT NULL,
                      sender_password character varying(255) COLLATE pg_catalog."default",
                      receiver_id character varying(50) COLLATE pg_catalog."default" NOT NULL,
                      receiver_organization_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
                      bic_code_pouring_bank character varying(255) COLLATE pg_catalog."default",
                      created timestamp(6) without time zone NOT NULL,
                      updated timestamp(6) without time zone,
                      published timestamp(6) without time zone,
                      CONSTRAINT flow_pk PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS flow_date_idx
    ON flow USING btree
    (date ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS flow_name_idx
    ON flow USING btree
    (name COLLATE pg_catalog."default" DESC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;
CREATE UNIQUE INDEX IF NOT EXISTS flow_revision_idx
    ON flow USING btree
    (psp_domain_id COLLATE pg_catalog."default" ASC NULLS LAST, name COLLATE pg_catalog."default" ASC NULLS LAST, revision ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS orgid_by_status_latest_idx
    ON flow USING btree
    (org_domain_id COLLATE pg_catalog."default" ASC NULLS LAST, status COLLATE pg_catalog."default" ASC NULLS LAST, is_latest ASC NULLS LAST, date ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS psp_flow_index
    ON flow USING btree
    (name COLLATE pg_catalog."default" ASC NULLS LAST, status COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS published_flow_by_organization_idx
    ON flow USING btree
    (org_domain_id COLLATE pg_catalog."default" ASC NULLS LAST, psp_domain_id COLLATE pg_catalog."default" ASC NULLS LAST, published ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS published_flow_by_psp_idx
    ON flow USING btree
    (psp_domain_id COLLATE pg_catalog."default" ASC NULLS LAST, org_domain_id COLLATE pg_catalog."default" ASC NULLS LAST, published ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;
-- CREATE UNIQUE INDEX flow_revision_idx ON flow USING btree (psp_domain_id, name, revision);
-- CREATE INDEX published_flow_by_organization_idx ON flow USING btree (org_domain_id, psp_domain_id, published);
-- CREATE INDEX published_flow_by_psp_idx ON flow USING btree (psp_domain_id, org_domain_id, published);

CREATE TABLE flow_to_history (
                                 id bigint NOT NULL DEFAULT nextval('flow_to_history_sequence'::regclass),
                                 psp_id character varying(50) COLLATE pg_catalog."default" NOT NULL,
                                 name character varying(255) COLLATE pg_catalog."default" NOT NULL,
                                 revision bigint NOT NULL,
                                 is_external boolean NOT NULL,
                                 created timestamp(6) without time zone,
                                 last_execution timestamp(6) without time zone,
                                 retries bigint NOT NULL DEFAULT 0,
                                 lock_until timestamp(6) without time zone,
                                 CONSTRAINT flow_to_history_pk PRIMARY KEY (id)
);
-- CREATE UNIQUE INDEX flow_to_historicization_idx ON flow_to_history USING btree (psp_id, name, revision);
CREATE UNIQUE INDEX IF NOT EXISTS flow_to_historicization_idx
    ON flow_to_history USING btree
    (psp_id COLLATE pg_catalog."default" ASC NULLS LAST, name COLLATE pg_catalog."default" ASC NULLS LAST, revision ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;


CREATE TABLE payment (
                         flow_id bigint NOT NULL,
                         iuv character varying(35) COLLATE pg_catalog."default" NOT NULL,
                         iur character varying(35) COLLATE pg_catalog."default" NOT NULL,
                         index bigint NOT NULL,
                         amount numeric(19,2) NOT NULL,
                         pay_date timestamp(6) without time zone NOT NULL,
                         pay_status character varying(50) COLLATE pg_catalog."default" NOT NULL,
                         transfer_id bigint NOT NULL,
                         created timestamp(6) without time zone,
                         updated timestamp(6) without time zone,
                         CONSTRAINT payment_pkey PRIMARY KEY (flow_id, index),
                         CONSTRAINT payment_flow_fk FOREIGN KEY (flow_id)
                             REFERENCES flow (id) MATCH SIMPLE
                             ON UPDATE CASCADE
                             ON DELETE CASCADE
);
-- CREATE UNIQUE INDEX payment_by_fdr_idx ON payment USING btree (flow_id, index);
CREATE INDEX IF NOT EXISTS payment_by_iur_idx
    ON payment USING btree
    (iur COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;
CREATE INDEX IF NOT EXISTS payment_by_iuv_idx
    ON payment USING btree
    (iuv COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;