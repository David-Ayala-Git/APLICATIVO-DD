# Respaldos de la base de datos

El servicio `db-backup` de `docker-compose.yml` guarda **un archivo por dia** con
toda la base (los esquemas de los 7 microservicios) en `BACKUP_DIR` del servidor
(por defecto `./backups`, junto al compose).

| Variable (.env)         | Defecto     | Que hace                                  |
|-------------------------|-------------|-------------------------------------------|
| `BACKUP_DIR`            | `./backups` | carpeta del servidor donde quedan          |
| `BACKUP_HOUR`           | `2`         | hora de Bogota del respaldo diario         |
| `BACKUP_RETENTION_DAYS` | `14`        | archivos mas viejos se borran              |

Al arrancar, si no hay un respaldo de las ultimas 24 h, hace uno de inmediato.
Cada archivo se valida con `pg_restore --list` antes de quedar con su nombre
final, asi que un `.dump` en la carpeta es un respaldo completo.

## Lo que NO cubre

- **Copia fuera del servidor.** Si el disco o el servidor se pierden, los
  respaldos se van con ellos. Programa una copia diaria de `BACKUP_DIR` a otro
  lugar (otro equipo, S3, Google Drive con `rclone`, etc.).
- **n8n.** Sus flujos y credenciales viven en el volumen `n8ndata`, no en
  Postgres. Los flujos de la plataforma se pueden reimportar desde
  `infra/n8n/workflows`; las credenciales hay que volver a crearlas.

## Ver los respaldos y hacer uno a mano

```bash
ls -lh backups/
docker compose logs --tail 20 db-backup
docker compose run --rm -e BACKUP_ONCE=1 db-backup     # respaldo inmediato
```

## Restaurar

> Restaurar **reemplaza** los datos actuales. Haz primero un respaldo a mano.

1. Detener los microservicios (Postgres queda arriba):

   ```bash
   docker compose stop api-gateway dd_ms_auth dd_ms_crm dd_ms_billing dd_ms_network \
     dd_ms_support dd_ms_comms dd_ms_reports n8n
   ```

2. Restaurar el archivo elegido (`--clean` borra y recrea cada objeto del respaldo):

   ```bash
   F=backups/ddconexiones-20260101-0200.dump
   docker compose exec -T postgres pg_restore -U postgres -d ddconexiones \
     --clean --if-exists --no-owner < "$F"
   ```

3. Levantar todo de nuevo: `docker compose up -d`.

Para recuperar **una sola tabla** sin tocar lo demas (p. ej. facturas borradas
por error), restaura en una base aparte y copia desde alli:

```bash
docker compose exec postgres createdb -U postgres rescate
docker compose exec -T postgres pg_restore -U postgres -d rescate --no-owner < "$F"
# ... consultar/copiar desde la base "rescate" ...
docker compose exec postgres dropdb -U postgres rescate
```

## Probar que los respaldos sirven

Un respaldo que nunca se restauro no es un respaldo. Una vez al mes, restaura el
ultimo archivo en la base `rescate` (arriba) y revisa que tenga los clientes y
facturas del dia anterior.
