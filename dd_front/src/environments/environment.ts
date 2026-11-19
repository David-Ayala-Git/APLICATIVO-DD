/**
 * Desarrollo: las llamadas salen como rutas relativas (/api/...) y el dev-server
 * de Angular las reenvia al gateway segun proxy.conf.json. Asi no hace falta CORS.
 */
export const environment = {
  production: false,
  /** Prefijo de la API. Vacio = mismo origen (proxy del dev-server). */
  apiBaseUrl: '',
  /** Consola de n8n, enlazada desde la pagina de Automatizacion. */
  n8nUrl: 'http://localhost:5678',
};
