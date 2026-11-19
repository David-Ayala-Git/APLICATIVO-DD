CREATE TABLE crm.plan (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id   VARCHAR(64)  NOT NULL,
  name        VARCHAR(120) NOT NULL,
  down_mbps   INTEGER      NOT NULL,
  up_mbps     INTEGER      NOT NULL,
  technology  VARCHAR(20)  NOT NULL,
  price       NUMERIC(12,2) NOT NULL,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE crm.client (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id   VARCHAR(64)  NOT NULL,
  full_name   VARCHAR(200) NOT NULL,
  document    VARCHAR(30)  NOT NULL,
  phone       VARCHAR(30),
  address     VARCHAR(250),
  plan_id     UUID REFERENCES crm.plan(id),
  status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  ip_address  VARCHAR(45),
  onu_serial  VARCHAR(40),
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT uq_client_doc UNIQUE (tenant_id, document)
);

CREATE INDEX idx_client_tenant ON crm.client(tenant_id);
CREATE INDEX idx_plan_tenant   ON crm.plan(tenant_id);