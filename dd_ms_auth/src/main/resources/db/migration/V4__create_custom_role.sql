-- Roles personalizados por proveedor. Conviven con el enum fijo de app_user.role
-- (SUPER_ADMIN/ADMIN_ISP/OPERADOR/TECNICO/CARTERA): el enum sigue decidiendo el
-- nivel administrativo del usuario, este rol personalizado afina que paginas ve
-- dentro de la consola (lista de claves de pagina en `permissions`).
CREATE TABLE dd_auth.custom_role (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id   UUID NOT NULL REFERENCES dd_auth.tenant(id),
  name        VARCHAR(80) NOT NULL,
  description VARCHAR(255),
  permissions JSONB NOT NULL DEFAULT '[]',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, name)
);

ALTER TABLE dd_auth.app_user
  ADD COLUMN custom_role_id UUID REFERENCES dd_auth.custom_role(id);

CREATE INDEX idx_custom_role_tenant ON dd_auth.custom_role(tenant_id);
CREATE INDEX idx_user_custom_role ON dd_auth.app_user(custom_role_id);
