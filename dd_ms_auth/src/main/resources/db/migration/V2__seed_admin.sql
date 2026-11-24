-- Usuario semilla para pruebas locales. Password en texto plano: Admin123!
-- Cambiar/eliminar antes de un despliegue real.
INSERT INTO dd_auth.app_user (email, password_hash, full_name, role, status)
VALUES (
  'admin@ddconexiones.co',
  '$2a$10$BCxGCYwUpVnGIgmyjM61wOvBg2cKFvWXQIx7jKU.PH8RQQmycW2B6',
  'Super Admin',
  'SUPER_ADMIN',
  'ACTIVE'
);
