CREATE SEQUENCE IF NOT EXISTS flow_sequence START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE IF NOT EXISTS payment_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE flow
(
    id                         BIGINT DEFAULT nextval('flow_sequence'::regclass) NOT NULL,
    name                       VARCHAR(255),
    date                       TIMESTAMP WITHOUT TIME ZONE,
    revision                   BIGINT,
    status                     VARCHAR(255),
    is_latest                  BOOLEAN,
    psp_domain_id              VARCHAR(255),
    org_domain_id              VARCHAR(255),
    tot_amount                 DECIMAL,
    tot_payments               BIGINT,
    computed_tot_amount        DECIMAL,
    computed_tot_payments      BIGINT,
    regulation                 VARCHAR(255),
    regulation_date            TIMESTAMP WITHOUT TIME ZONE,
    sender_id                  VARCHAR(255),
    sender_psp_broker_id       VARCHAR(255),
    sender_channel_id          VARCHAR(255),
    sender_password            VARCHAR(255),
    sender_psp_name            VARCHAR(255),
    sender_type                VARCHAR(255),
    receiver_id                VARCHAR(255),
    receiver_organization_name VARCHAR(255),
    bic_code_pouring_bank      VARCHAR(255),
    created                    TIMESTAMP WITHOUT TIME ZONE,
    updated                    TIMESTAMP WITHOUT TIME ZONE,
    published                  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_flow PRIMARY KEY (id)
);

CREATE TABLE payment
(
    id          BIGINT DEFAULT nextval('payment_sequence'::regclass) NOT NULL,
    flow_id     BIGINT,
    iuv         VARCHAR(255),
    iur         VARCHAR(255),
    index       BIGINT,
    amount      DECIMAL,
    pay_date    TIMESTAMP WITHOUT TIME ZONE,
    pay_status  VARCHAR(255),
    transfer_id BIGINT,
    created     TIMESTAMP WITHOUT TIME ZONE,
    updated     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_payment PRIMARY KEY (id)
);

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_FLOW FOREIGN KEY (flow_id) REFERENCES flow (id) on delete cascade;