ALTER TABLE network.node
  ADD COLUMN IF NOT EXISTS nap_capacity INTEGER;

UPDATE network.node
   SET nap_capacity = 8
 WHERE type = 'NAP' AND nap_capacity IS NULL;

CREATE TABLE IF NOT EXISTS network.nap_port_assignment (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id   VARCHAR(64) NOT NULL,
  node_id     UUID NOT NULL REFERENCES network.node(id) ON DELETE CASCADE,
  client_id   UUID NOT NULL,
  port_number INTEGER NOT NULL,
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_nap_port UNIQUE (node_id, port_number),
  CONSTRAINT uq_nap_client_tenant UNIQUE (tenant_id, client_id)
);

CREATE INDEX IF NOT EXISTS idx_nap_assignment_tenant ON network.nap_port_assignment(tenant_id);
CREATE INDEX IF NOT EXISTS idx_nap_assignment_node   ON network.nap_port_assignment(node_id);
