--liquibase formatted sql

-- ## SEQUENCES ##
--changeset liquibase:202603091000-01
CREATE SEQUENCE fdr3.flow_sequence
       INCREMENT BY 1
       MINVALUE 1
       MAXVALUE 9223372036854775807
       START 1
       CACHE 1
       NO CYCLE;

--changeset liquibase:202603091000-02
CREATE SEQUENCE fdr3.payment_sequence
       INCREMENT BY 1
       MINVALUE 1
       MAXVALUE 9223372036854775807
       START 1
       CACHE 1
       NO CYCLE;
	
-- ## TABLES ##
--changeset liquibase:202603091000-03
CREATE TABLE IF NOT EXISTS fdr3.flow (
    id BIGINT NOT NULL DEFAULT nextval('flow_sequence'::regclass),
    name CHARACTER VARYING(255) NOT NULL,
    "date" TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    revision BIGINT NOT NULL,
    status CHARACTER VARYING(50) NOT NULL,
    is_latest BOOLEAN NOT NULL DEFAULT false,
    psp_domain_id CHARACTER VARYING(50) NOT NULL,
    org_domain_id CHARACTER VARYING(50) NOT NULL,
    tot_payments BIGINT NOT NULL DEFAULT 0,
    tot_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    computed_tot_payments BIGINT NOT NULL DEFAULT 0,
    computed_tot_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    regulation CHARACTER VARYING(255) NOT NULL,
    regulation_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    sender_id CHARACTER VARYING(50) NOT NULL,
    sender_type CHARACTER VARYING(50) NOT NULL,
    sender_psp_name CHARACTER VARYING(255) NOT NULL,
    sender_psp_broker_id CHARACTER VARYING(50) NOT NULL,
    sender_channel_id CHARACTER VARYING(50) NOT NULL,
    sender_password CHARACTER VARYING(255),
    receiver_id CHARACTER VARYING(50) NOT NULL,
    receiver_organization_name CHARACTER VARYING(255) NOT NULL,
    bic_code_pouring_bank CHARACTER VARYING(255),
    created TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated TIMESTAMP(6) WITHOUT TIME ZONE,
    published TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT flow_pk PRIMARY KEY (id)
)
PARTITION BY RANGE ("date");

--changeset liquibase:202603091000-04
CREATE TABLE IF NOT EXISTS fdr3.payment (
    flow_id BIGINT NOT NULL,
    index BIGINT NOT NULL,
    flow_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    iuv CHARACTER VARYING(35) NOT NULL,
    iur CHARACTER VARYING(35) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    pay_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    pay_status CHARACTER VARYING(50) NOT NULL,
    transfer_id BIGINT NOT NULL,
    created TIMESTAMP(6) WITHOUT TIME ZONE,
    updated TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT payment_pk PRIMARY KEY (flow_id, index, flow_date)
)
PARTITION BY RANGE (flow_date);

-- ## FOREIGN KEYS ##
--changeset liquibase:202603091000-05
ALTER TABLE fdr3.payment
  ADD CONSTRAINT payment_flow_fk
      FOREIGN KEY (flow_id)
      REFERENCES fdr3.flow (id) MATCH SIMPLE
      ON UPDATE CASCADE
      ON DELETE CASCADE;

-- ## INDEXES ##
--changeset liquibase:202603091000-06
CREATE UNIQUE INDEX IF NOT EXISTS flow_revision_idx
    ON fdr3.flow
 USING btree (psp_domain_id, "name", revision);

--changeset liquibase:202603091000-07
CREATE INDEX IF NOT EXISTS flow_date_idx
    ON fdr3.flow
 USING btree ("date");

--changeset liquibase:202603091000-08
CREATE INDEX IF NOT EXISTS published_flow_by_organization_idx
    ON fdr3.flow
 USING btree (org_domain_id, psp_domain_id, published);

--changeset liquibase:202603091000-09
CREATE INDEX IF NOT EXISTS published_flow_by_psp_idx
    ON fdr3.flow
 USING btree (psp_domain_id, org_domain_id, published);

--changeset liquibase:202603091000-10
CREATE INDEX IF NOT EXISTS psp_flow_index
    ON fdr3.flow
 USING btree ("name", "status");

--changeset liquibase:202603091000-11
CREATE INDEX IF NOT EXISTS orgid_by_status_latest_idx
    ON fdr3.flow
 USING btree (org_domain_id, status, is_latest, "date");

--changeset liquibase:202603091000-12
CREATE INDEX IF NOT EXISTS payment_by_iur_idx
    ON fdr3.payment
 USING btree (iur);

--changeset liquibase:202603091000-13
CREATE INDEX IF NOT EXISTS payment_by_iuv_idx
    ON fdr3.payment
 USING btree (iuv);
