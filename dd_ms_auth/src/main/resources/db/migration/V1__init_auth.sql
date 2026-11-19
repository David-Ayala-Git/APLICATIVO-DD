CREATE TABLE auth.tenant (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name          VARCHAR(120) NOT NULL,
  slug          VARCHAR(60)  NOT NULL UNIQUE,
  plan          VARCHAR(30)  NOT NULL DEFAULT 'TRIAL',
  status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  brand_primary VARCHAR(7)   NOT NULL DEFAULT '#6d28d9',
  brand_accent  VARCHAR(7)   NOT NULL DEFAULT '#06b6d4',
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE auth.tenant_module (
  tenant_id  UUID NOT NULL REFERENCES auth.tenant(id),
  module_key VARCHAR(40) NOT NULL,
  enabled    BOOLEAN NOT NULL DEFAULT false,
  config     JSONB,
  PRIMARY KEY (tenant_id, module_key)
);

CREATE TABLE auth.app_user (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     UUID REFERENCES auth.tenant(id),
  email         VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(200) NOT NULL,
  full_name     VARCHAR(160) NOT NULL,
  role          VARCHAR(30)  NOT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_tenant_slug ON auth.tenant(slug);
CREATE INDEX idx_user_tenant ON auth.app_user(tenant_id);
CREATE INDEX idx_user_email ON auth.app_user(email);
