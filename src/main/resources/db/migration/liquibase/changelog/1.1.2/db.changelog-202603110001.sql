--liquibase formatted sql

-- ## VIEWS ##
--changeset liquibase:admin-202603110002-01
CREATE MATERIALIZED VIEW fdr3.mview_flows_published_last_day AS
SELECT *
  FROM fdr3.flow
 WHERE published >= Date_trunc('day', Now()::DATE - INTERVAL '1 day')
       AND published < Date_trunc('day', Now()::DATE)
  WITH NO DATA;

CREATE MATERIALIZED VIEW fdr3.mview_payments_published_last_day AS
SELECT payment.*
       ,published_flows.date AS flow_date
  FROM fdr3.payment AS payment
       JOIN fdr3.mview_flows_published_last_day AS published_flows
         ON payment.flow_id = published_flows.id
  WITH NO DATA;

-- ## INDEXES ##
--changeset liquibase:admin-202603110001-02
CREATE UNIQUE INDEX flows_published_last_day_idx
       ON fdr3.mview_flows_published_last_day
       USING btree (id ASC)
       INCLUDE (date)
       WITH (fillfactor=100, deduplicate_items=True);