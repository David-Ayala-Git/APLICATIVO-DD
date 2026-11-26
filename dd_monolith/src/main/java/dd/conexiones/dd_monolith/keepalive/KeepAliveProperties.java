package dd.conexiones.dd_monolith.keepalive;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion externalizada del mecanismo Keep-Alive.
 * Se enlaza con las variables de entorno KEEP_ALIVE_ENABLED, KEEP_ALIVE_URL
 * y KEEP_ALIVE_INTERVAL (en milisegundos).
 */
@ConfigurationProperties(prefix = "keep-alive")
public class KeepAliveProperties {

    /** Habilita o deshabilita el mecanismo Keep-Alive. */
    private boolean enabled = false;

    /** URL publica completa del endpoint /health (ej. https://aplicativo-dd.onrender.com/health). */
    private String url = "";

    /** Intervalo entre peticiones en milisegundos (por defecto 12 minutos = 720000 ms). */
    private long intervalMs = 720_000L;

    /** Tiempo maximo de espera por peticion HTTP en milisegundos. */
    private long timeoutMs = 15_000L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
