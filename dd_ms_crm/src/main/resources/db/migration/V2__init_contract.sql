CREATE TABLE crm.contract (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     VARCHAR(64)  NOT NULL,
  client_id     UUID NOT NULL REFERENCES crm.client(id),
  plan_id       UUID NOT NULL REFERENCES crm.plan(id),
  status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
  signed_at     TIMESTAMPTZ,
  document_url  VARCHAR(500),
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_contract_tenant ON crm.contract(tenant_id);
CREATE INDEX idx_contract_client ON crm.contract(client_id);
