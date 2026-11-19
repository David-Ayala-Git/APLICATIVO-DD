#!/bin/sh
# Respaldo diario de PostgreSQL (todos los esquemas de la plataforma en un solo
# archivo). Corre dentro del contenedor db-backup de docker-compose.yml.
#
#   BACKUP_HOUR            hora local (0-23) del respaldo diario. Defecto 2.
#   BACKUP_RETENTION_DAYS  dias que se conservan los archivos. Defecto 14.
#   BACKUP_ONCE=1          hace un solo respaldo y termina (pruebas / manual).
#
# Formato custom de pg_dump (-Fc): comprimido y restaurable por esquema o tabla
# con pg_restore. Ver infra/backup/README.md para restaurar.
set -eu

HOUR="${BACKUP_HOUR:-2}"
RETENTION="${BACKUP_RETENTION_DAYS:-14}"
DIR=/backups

backup() {
  stamp=$(date +%Y%m%d-%H%M)
  file="$DIR/${PGDATABASE}-${stamp}.dump"
  # se escribe en .tmp y se valida antes de renombrar: un respaldo a medias
  # nunca queda con nombre de respaldo bueno
  pg_dump -Fc --no-owner -f "$file.tmp"
  pg_restore --list "$file.tmp" > /dev/null
  mv "$file.tmp" "$file"
  echo "$(date '+%F %T') respaldo OK: $file ($(du -h "$file" | cut -f1))"
  find "$DIR" -name "${PGDATABASE}-*.dump" -mtime +"$RETENTION" -print -delete
  find "$DIR" -name '*.tmp' -mmin +120 -delete
}

if [ "${BACKUP_ONCE:-0}" = "1" ]; then
  backup
  exit 0
fi

# Al arrancar, si el ultimo respaldo tiene mas de un dia (o no hay), se hace uno.
if [ -z "$(find "$DIR" -name "${PGDATABASE}-*.dump" -mmin -1440 | head -1)" ]; then
  backup || echo "$(date '+%F %T') ERROR en el respaldo inicial" >&2
fi

while true; do
  now=$(date +%s)
  next=$(date -d "today ${HOUR}:00" +%s)
  [ "$next" -le "$now" ] && next=$(date -d "tomorrow ${HOUR}:00" +%s)
  sleep $((next - now))
  # un fallo (p. ej. postgres reiniciando) no tumba el ciclo: se reintenta manana
  backup || echo "$(date '+%F %T') ERROR en el respaldo" >&2
done
