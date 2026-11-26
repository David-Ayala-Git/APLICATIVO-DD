-- Codigo unico consecutivo por cliente (formato 0001, 0002, ...) y estado habilitado en planes.
ALTER TABLE crm.client
    ADD COLUMN IF NOT EXISTS client_code VARCHAR(16);

WITH ranked AS (
    SELECT id,
           LPAD(ROW_NUMBER() OVER (PARTITION BY tenant_id ORDER BY created_at, id)::text, 4, '0') AS gen_code
    FROM crm.client
    WHERE client_code IS NULL OR btrim(client_code) = ''
)
UPDATE crm.client c
SET client_code = r.gen_code
FROM ranked r
WHERE c.id = r.id;

ALTER TABLE crm.client
    ALTER COLUMN client_code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_client_tenant_code
    ON crm.client (tenant_id, client_code);

ALTER TABLE crm.plan
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;
