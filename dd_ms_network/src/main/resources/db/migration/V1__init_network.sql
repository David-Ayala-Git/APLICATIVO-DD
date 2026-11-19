CREATE TABLE network.router (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        VARCHAR(64)  NOT NULL,
  name             VARCHAR(120) NOT NULL,
  host             VARCHAR(100) NOT NULL,
  api_port         INTEGER      NOT NULL DEFAULT 8728,
  api_user         VARCHAR(80)  NOT NULL,
  api_password_enc VARCHAR(500) NOT NULL,
  model            VARCHAR(80),
  status           VARCHAR(20)  NOT NULL DEFAULT 'OFFLINE',
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE network.onu (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id   VARCHAR(64)  NOT NULL,
  serial      VARCHAR(60)  NOT NULL,
  vendor      VARCHAR(20)  NOT NULL,
  olt_name    VARCHAR(120) NOT NULL,
  pon_port    VARCHAR(20)  NOT NULL,
  rx_power    DOUBLE PRECISION,
  status      VARCHAR(20)  NOT NULL DEFAULT 'UNAUTHORIZED',
  client_id   UUID,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT uq_onu_serial UNIQUE (tenant_id, serial)
);

CREATE TABLE network.equipment (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        VARCHAR(64)  NOT NULL,
  type             VARCHAR(20)  NOT NULL,
  brand            VARCHAR(80),
  model            VARCHAR(80),
  serial_number    VARCHAR(80)  NOT NULL,
  status           VARCHAR(20)  NOT NULL DEFAULT 'IN_STOCK',
  assigned_client_id UUID,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT uq_equipment_serial UNIQUE (tenant_id, serial_number)
);

CREATE TABLE network.node (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id   VARCHAR(64)  NOT NULL,
  name        VARCHAR(120) NOT NULL,
  type        VARCHAR(20)  NOT NULL,
  latitude    DOUBLE PRECISION NOT NULL,
  longitude   DOUBLE PRECISION NOT NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_router_tenant    ON network.router(tenant_id);
CREATE INDEX idx_onu_tenant       ON network.onu(tenant_id);
CREATE INDEX idx_onu_client       ON network.onu(client_id);
CREATE INDEX idx_equipment_tenant ON network.equipment(tenant_id);
CREATE INDEX idx_node_tenant      ON network.node(tenant_id);
