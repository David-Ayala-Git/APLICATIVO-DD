/**
 * Produccion: si el front se sirve detras del mismo dominio que el gateway,
 * deja apiBaseUrl vacio. Si se sirve aparte, apunta al gateway y habilita CORS
 * alli (spring.cloud.gateway.server.webflux.globalcors en dd_ms_gateway).
 */
export const environment = {
  production: true,
  apiBaseUrl: '',
  // n8n no se publica (docker-compose: 127.0.0.1:5678); se abre con un tunel SSH:
  //   ssh -L 5678:localhost:5678 usuario@servidor
  n8nUrl: 'http://localhost:5678',
};
