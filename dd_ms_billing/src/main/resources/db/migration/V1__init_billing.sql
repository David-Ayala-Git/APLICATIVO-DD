CREATE TABLE billing.invoice (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id   VARCHAR(64)  NOT NULL,
  client_id   UUID NOT NULL,
  plan_id     UUID,
  period      VARCHAR(7)   NOT NULL,
  amount      NUMERIC(12,2) NOT NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  due_date    DATE NOT NULL,
  issued_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
  cufe        VARCHAR(100),
  pdf_url     VARCHAR(500),
  xml_url     VARCHAR(500),
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT uq_invoice_period UNIQUE (tenant_id, client_id, period)
);

CREATE TABLE billing.payment (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id          VARCHAR(64) NOT NULL,
  invoice_id         UUID NOT NULL REFERENCES billing.invoice(id),
  amount             NUMERIC(12,2) NOT NULL,
  method             VARCHAR(20) NOT NULL,
  status             VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  external_reference VARCHAR(120),
  paid_at            TIMESTAMPTZ,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoice_tenant  ON billing.invoice(tenant_id);
CREATE INDEX idx_invoice_client  ON billing.invoice(client_id);
CREATE INDEX idx_payment_tenant  ON billing.payment(tenant_id);
CREATE INDEX idx_payment_invoice ON billing.payment(invoice_id);
