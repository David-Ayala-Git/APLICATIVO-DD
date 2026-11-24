#!/bin/sh
# =============================================================================
#  Arranque del ecosistema completo en UN contenedor (Render, Dockerfile raiz)
# =============================================================================
# - 7 microservicios en segundo plano, cada uno en su puerto interno (localhost).
# - api-gateway en $PORT (10000 en Render): es el unico puerto expuesto y el que
#   Render revisa con healthCheckPath=/actuator/health.
# - Vigilancia: si CUALQUIER JVM muere, se detienen todas y el contenedor sale
#   con error, asi Render lo reinicia en vez de dejarlo "vivo" pero roto.
set -eu

PORT="${PORT:-10000}"

# ---- Spring en modo Render: sin Consul, nombres resueltos a localhost ----
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-pdn,render}"
export SPRING_CLOUD_CONSUL_ENABLED=false
export SPRING_CLOUD_CONSUL_CONFIG_ENABLED=false
export CONSUL_HOST="${CONSUL_HOST:-localhost}"
# Todos viven en este contenedor: se fuerzan a localhost aunque el .env traiga
# otros valores (application-render.yml los usa para lb://dd-ms-*).
export AUTH_HOSTPORT=localhost:8081
export CRM_HOSTPORT=localhost:8082
export BILLING_HOSTPORT=localhost:8083
export NETWORK_HOSTPORT=localhost:8084
export SUPPORT_HOSTPORT=localhost:8085
export COMMS_HOSTPORT=localhost:8086
export REPORTS_HOSTPORT=localhost:8087

# ---- Memoria: 8 JVM comparten la RAM del contenedor ----
# -XX:MaxRAMPercentage=75 NO sirve aqui: cada JVM creeria que puede usar el 75%
# de TODA la RAM y entre las 8 el contenedor se quedaria sin memoria. Cada una
# recibe un tope fijo de heap (ajustable con SERVICE_HEAP / GATEWAY_HEAP).
unset JAVA_TOOL_OPTIONS
JVM_COMMON="-XX:+UseContainerSupport -XX:+UseSerialGC -XX:TieredStopAtLevel=1 \
 -Xss512k -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=48m \
 -XX:+ExitOnOutOfMemoryError ${JAVA_EXTRA_OPTS:-}"
SERVICE_HEAP="${SERVICE_HEAP:--Xmx192m}"
GATEWAY_HEAP="${GATEWAY_HEAP:--Xmx192m}"

PIDS=""

start() {   # start <jar> <puerto> <heap>
  echo "[entrypoint] arrancando $1 en :$2"
  # --server.port (linea de comandos) tiene prioridad sobre PORT/SERVER_PORT
  # shellcheck disable=SC2086
  java $JVM_COMMON $3 -jar "/app/$1.jar" --server.port="$2" &
  PIDS="$PIDS $!"
}

stop_all() {
  echo "[entrypoint] deteniendo todos los servicios"
  # shellcheck disable=SC2086
  kill $PIDS 2>/dev/null || true
  wait 2>/dev/null || true
}
trap 'stop_all; exit 0' TERM INT

start auth    8081 "$SERVICE_HEAP"
start crm     8082 "$SERVICE_HEAP"
start billing 8083 "$SERVICE_HEAP"
start network 8084 "$SERVICE_HEAP"
start support 8085 "$SERVICE_HEAP"
start comms   8086 "$SERVICE_HEAP"
start reports 8087 "$SERVICE_HEAP"
start gateway "$PORT" "$GATEWAY_HEAP"

# ---- Vigilancia ----
while :; do
  for pid in $PIDS; do
    if ! kill -0 "$pid" 2>/dev/null; then
      echo "[entrypoint] el proceso $pid termino inesperadamente" >&2
      stop_all
      exit 1
    fi
  done
  sleep 5
done
