CREATE TABLE comms.whatsapp_message (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id      VARCHAR(64)  NOT NULL,
  phone          VARCHAR(30)  NOT NULL,
  direction      VARCHAR(10)  NOT NULL,
  body           TEXT,
  template_name  VARCHAR(120),
  status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  sent_at        TIMESTAMPTZ,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE comms.ai_agent (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id      VARCHAR(64)  NOT NULL,
  name           VARCHAR(120) NOT NULL,
  channel        VARCHAR(30)  NOT NULL,
  system_prompt  TEXT         NOT NULL,
  tools          VARCHAR(500),
  enabled        BOOLEAN      NOT NULL DEFAULT true,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE comms.ai_message (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id    VARCHAR(64)  NOT NULL,
  agent_id     UUID NOT NULL REFERENCES comms.ai_agent(id),
  session_id   VARCHAR(120) NOT NULL,
  role         VARCHAR(20)  NOT NULL,
  content      TEXT         NOT NULL,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_whatsapp_tenant ON comms.whatsapp_message(tenant_id);
CREATE INDEX idx_agent_tenant    ON comms.ai_agent(tenant_id);
CREATE INDEX idx_aimsg_session   ON comms.ai_message(tenant_id, agent_id, session_id);
