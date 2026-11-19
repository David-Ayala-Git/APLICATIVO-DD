CREATE TABLE reports.executive_snapshot (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       VARCHAR(64)   NOT NULL,
  period          VARCHAR(7)    NOT NULL,
  mrr             NUMERIC(14,2) NOT NULL,
  arpu            NUMERIC(14,2) NOT NULL,
  churn_rate      NUMERIC(6,4)  NOT NULL,
  active_clients  INTEGER       NOT NULL,
  retired_clients INTEGER       NOT NULL,
  total_clients   INTEGER       NOT NULL,
  generated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
  CONSTRAINT uq_snapshot_period UNIQUE (tenant_id, period)
);

CREATE INDEX idx_snapshot_tenant ON reports.executive_snapshot(tenant_id);
