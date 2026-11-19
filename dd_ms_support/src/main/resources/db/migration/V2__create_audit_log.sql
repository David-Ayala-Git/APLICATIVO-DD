CREATE TABLE support.audit_log (
  id          UUID PRIMARY KEY,
  tenant_id   VARCHAR(60),
  user_id     VARCHAR(60),
  user_role   VARCHAR(30),
  action      VARCHAR(80)  NOT NULL,
  detail      TEXT,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_support_audit_log_tenant ON support.audit_log(tenant_id);
CREATE INDEX idx_support_audit_log_action ON support.audit_log(action);
