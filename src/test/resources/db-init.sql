CREATE SEQUENCE IF NOT EXISTS flow_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS payment_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS flow_to_history_sequence START WITH 1 INCREMENT BY 1;


CREATE TABLE flow (
                           id numeric DEFAULT nextval('flow_sequence'::regclass) NOT NULL,
                           "name" varchar(255) NOT NULL,
                           "date" timestamp(6) NOT NULL,
                           revision numeric(19) NOT NULL,
                           status varchar(50) NOT NULL,
                           is_latest bool DEFAULT false NOT NULL,
                           psp_domain_id varchar(50) NOT NULL,
                           org_domain_id varchar(50) NOT NULL,
                           tot_payments numeric(19) DEFAULT 0 NOT NULL,
                           tot_amount float4 DEFAULT 0 NOT NULL,
                           computed_tot_payments numeric(19) DEFAULT 0 NOT NULL,
                           computed_tot_amount float4 DEFAULT 0 NOT NULL,
                           regulation varchar(255) NOT NULL,
                           regulation_date timestamp(6) NOT NULL,
                           sender_id varchar(50) NOT NULL,
                           sender_type varchar(50) NOT NULL,
                           sender_psp_name varchar(255) NOT NULL,
                           sender_psp_broker_id varchar(50) NOT NULL,
                           sender_channel_id varchar(50) NOT NULL,
                           sender_password varchar(255) NULL,
                           receiver_id varchar(50) NOT NULL,
                           receiver_organization_name varchar(255) NOT NULL,
                           bic_code_pouring_bank varchar(255) NULL,
                           created timestamp(6) NOT NULL,
                           updated timestamp(6) NULL,
                           published timestamp(6) NULL,
                           CONSTRAINT flow_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX flow_revision_idx ON flow USING btree (psp_domain_id, name, revision);
CREATE INDEX published_flow_by_organization_idx ON flow USING btree (org_domain_id, psp_domain_id, published);
CREATE INDEX published_flow_by_psp_idx ON flow USING btree (psp_domain_id, org_domain_id, published);

CREATE TABLE flow_to_history (
                                      id numeric DEFAULT nextval('flow_to_history_sequence'::regclass) NOT NULL,
                                      psp_id varchar(50) NOT NULL,
                                      "name" varchar(255) NOT NULL,
                                      revision numeric(19) NOT NULL,
                                      is_external bool NOT NULL,
                                      created timestamp(6) NULL,
                                      last_execution timestamp(6) NULL,
                                      retries numeric(19) DEFAULT 0 NOT NULL,
                                      CONSTRAINT flow_to_history_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX flow_to_historicization_idx ON flow_to_history USING btree (psp_id, name, revision);


CREATE TABLE payment (
                              id numeric DEFAULT nextval('payment_sequence'::regclass) NOT NULL,
                              flow_id numeric NOT NULL,
                              iuv varchar(50) NOT NULL,
                              iur varchar(50) NOT NULL,
                              "index" numeric(19) NOT NULL,
                              amount float4 NOT NULL,
                              pay_date timestamp(6) NOT NULL,
                              pay_status varchar(50) NOT NULL,
                              transfer_id numeric(19) NOT NULL,
                              created timestamp(6) NULL,
                              updated timestamp(6) NULL,
                              CONSTRAINT payment_pk PRIMARY KEY (id),
                              CONSTRAINT payment_flow_fk FOREIGN KEY (flow_id) REFERENCES flow(id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE UNIQUE INDEX payment_by_fdr_idx ON payment USING btree (flow_id, index);