-- crm, billing y contracts pasan a validarse con @RequiresModule en sus
-- microservicios. Un tenant sin fila en tenant_module se trata como modulo
-- apagado, asi que sin esto los tenants existentes perderian el nucleo.
-- ON CONFLICT respeta a quien ya lo apago a proposito desde Modulos.
INSERT INTO dd_auth.tenant_module (tenant_id, module_key, enabled)
SELECT t.id, m.module_key, true
FROM dd_auth.tenant t
CROSS JOIN (VALUES ('crm'), ('billing'), ('contracts')) AS m(module_key)
ON CONFLICT (tenant_id, module_key) DO NOTHING;
