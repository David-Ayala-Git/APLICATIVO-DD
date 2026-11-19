# Base de datos en Supabase

1. Crea el proyecto en Supabase en la region **East US (North Virginia)**, la misma de Render.
2. En **SQL Editor** ejecuta una sola vez `infra/postgres/init/01-schemas.sql`
   (crea los schemas auth, crm, billing, network, support, comms y reports).
   Las tablas las crea Flyway al arrancar cada microservicio.
3. En **Connect > Session pooler** copia los datos y cargalos en Render:

   | Variable      | Valor |
   |---------------|-------|
   | `DB_URL`      | `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require` (usa el host que te muestre Supabase) |
   | `DB_USER`     | `postgres.<ref-del-proyecto>` |
   | `DB_PASSWORD` | la clave de la base |

Usa el **Session pooler (puerto 5432)**, no el Transaction pooler (6543): Hibernate
y Flyway usan sentencias preparadas y bloqueos de sesion. La conexion directa
(`db.<ref>.supabase.co`) es solo IPv6 y Render no la alcanza.

El pooler gratis admite 15 conexiones: por eso render.yaml fija `DB_POOL_SIZE=2`
(7 servicios x 2 = 14). Si agregas servicios o replicas, sube el plan de Supabase.
