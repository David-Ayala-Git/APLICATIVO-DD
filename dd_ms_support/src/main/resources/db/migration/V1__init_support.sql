CREATE TABLE support.technician (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id   VARCHAR(64)  NOT NULL,
  full_name   VARCHAR(160) NOT NULL,
  phone       VARCHAR(30),
  status      VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE support.ticket (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id    VARCHAR(64)  NOT NULL,
  client_id    UUID NOT NULL,
  reason       VARCHAR(500) NOT NULL,
  priority     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
  status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
  assigned_to  UUID REFERENCES support.technician(id),
  sla_due_at   TIMESTAMPTZ  NOT NULL,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE support.work_order (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id      VARCHAR(64)  NOT NULL,
  client_id      UUID NOT NULL,
  ticket_id      UUID REFERENCES support.ticket(id),
  type           VARCHAR(20)  NOT NULL,
  technician_id  UUID REFERENCES support.technician(id),
  scheduled_at   TIMESTAMPTZ,
  address        VARCHAR(250),
  status         VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_technician_tenant ON support.technician(tenant_id);
CREATE INDEX idx_ticket_tenant     ON support.ticket(tenant_id);
CREATE INDEX idx_ticket_client     ON support.ticket(client_id);
CREATE INDEX idx_workorder_tenant  ON support.work_order(tenant_id);
CREATE INDEX idx_workorder_client  ON support.work_order(client_id);
