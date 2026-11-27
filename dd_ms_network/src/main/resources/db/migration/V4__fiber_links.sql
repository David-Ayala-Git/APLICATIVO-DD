CREATE TABLE IF NOT EXISTS network.fiber_link (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id      VARCHAR(64) NOT NULL,
  name           VARCHAR(120),
  from_node_id   UUID NOT NULL REFERENCES network.node(id) ON DELETE CASCADE,
  to_node_id     UUID NOT NULL REFERENCES network.node(id) ON DELETE CASCADE,
  fiber_type     VARCHAR(30) NOT NULL DEFAULT 'DISTRIBUTION',
  color          VARCHAR(20) NOT NULL DEFAULT '#06b6d4',
  waypoints_data TEXT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_fiber_link_tenant ON network.fiber_link(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fiber_link_from   ON network.fiber_link(from_node_id);
CREATE INDEX IF NOT EXISTS idx_fiber_link_to     ON network.fiber_link(to_node_id);
