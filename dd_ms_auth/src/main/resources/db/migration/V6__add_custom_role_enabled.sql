-- Permite deshabilitar un rol personalizado sin borrarlo (p. ej. cuando ya
-- tiene usuarios asignados o se quiere pausar temporalmente su alcance).
ALTER TABLE dd_auth.custom_role
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;
