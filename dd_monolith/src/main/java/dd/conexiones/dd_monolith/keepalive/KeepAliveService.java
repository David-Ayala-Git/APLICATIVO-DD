package dd.conexiones.dd_monolith.keepalive;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mecanismo automatico de Keep-Alive independiente de la logica de negocio.
 *
 * Envia solicitudes HTTP GET periodicas al endpoint publico /health para reducir
 * suspensiones por inactividad en Render. Garantiza:
 * - Una sola instancia de temporizador por JVM (sin duplicados).
 * - Tolerancia total a fallos de red o timeouts (nunca detiene ejecuciones futuras).
 * - Apagado ordenado al detener la aplicacion (@PreDestroy).
 */
@Component
@EnableConfigurationProperties(KeepAliveProperties.class)
public class KeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveService.class);
    private static final long MIN_INTERVAL_MS = 50L;
    private static final long MIN_TIMEOUT_MS = 200L;

    private final KeepAliveProperties properties;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong totalAttempts = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);

    private volatile ScheduledExecutorService scheduler;
    private volatile ScheduledFuture<?> scheduledTask;
    private volatile HttpClient httpClient;
    private volatile URI targetUri;

    public KeepAliveService(KeepAliveProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        start();
    }

    /**
     * Inicia el temporizador periodico si KEEP_ALIVE_ENABLED=true y la configuracion es valida.
     * Es idempotente: multiples llamadas nunca crean temporizadores duplicados.
     */
    public synchronized boolean start() {
        if (!properties.isEnabled()) {
            log.info("[Keep-Alive] Deshabilitado (KEEP_ALIVE_ENABLED=false). No se iniciara el temporizador.");
            return false;
        }
        if (running.get()) {
            log.debug("[Keep-Alive] Ya se encuentra activo; se omite inicio duplicado.");
            return false;
        }

        URI parsed = parseValidUri(properties.getUrl());
        if (parsed == null) {
            log.warn("[Keep-Alive] URL invalida o vacia ('{}'). Configura KEEP_ALIVE_URL con http(s)://...",
                    properties.getUrl());
            return false;
        }

        long intervalMs = properties.getIntervalMs();
        if (intervalMs < MIN_INTERVAL_MS) {
            log.warn("[Keep-Alive] Intervalo invalido ({} ms). Debe ser >= {} ms.", intervalMs, MIN_INTERVAL_MS);
            return false;
        }

        long timeoutMs = Math.max(MIN_TIMEOUT_MS, properties.getTimeoutMs());
        this.targetUri = parsed;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "keep-alive-scheduler");
            thread.setDaemon(true);
            return thread;
        });

        this.scheduledTask = this.scheduler.scheduleWithFixedDelay(
                this::pingSafely,
                intervalMs,
                intervalMs,
                TimeUnit.MILLISECONDS
        );
        this.running.set(true);

        log.info("[Keep-Alive] Iniciado -> url={} | intervalo={} ms ({} min) | timeout={} ms",
                parsed, intervalMs, String.format("%.1f", intervalMs / 60_000.0), timeoutMs);
        return true;
    }

    /**
     * Ejecuta una solicitud GET al endpoint configurado capturando cualquier excepcion
     * para garantizar que el ciclo programado nunca se interrumpa.
     */
    public boolean pingSafely() {
        URI uri = this.targetUri != null ? this.targetUri : parseValidUri(properties.getUrl());
        if (uri == null) {
            log.warn("[Keep-Alive] {} | Omitido: URL no configurada.", nowIso());
            return false;
        }

        HttpClient client = this.httpClient;
        long timeoutMs = Math.max(MIN_TIMEOUT_MS, properties.getTimeoutMs());
        if (client == null) {
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        }

        totalAttempts.incrementAndGet();
        long startNanos = System.nanoTime();
        String timestamp = nowIso();

        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .GET()
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("User-Agent", "dd-monolith-keep-alive/1.0")
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            int status = response.statusCode();

            if (status >= 200 && status < 300) {
                successCount.incrementAndGet();
                log.info("[Keep-Alive] {} | OK -> GET {} | status={} | latency={} ms",
                        timestamp, uri, status, elapsedMs);
                return true;
            }

            failureCount.incrementAndGet();
            log.warn("[Keep-Alive] {} | RESPUESTA NO EXITOSA -> GET {} | status={} | latency={} ms",
                    timestamp, uri, status, elapsedMs);
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            failureCount.incrementAndGet();
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            log.warn("[Keep-Alive] {} | INTERRUMPIDO -> GET {} | latency={} ms | detalle={}",
                    timestamp, uri, elapsedMs, ex.getMessage());
            return false;
        } catch (Throwable ex) {
            failureCount.incrementAndGet();
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            String detail = ex.getMessage();
            if ((detail == null || detail.isBlank()) && ex.getCause() != null) {
                detail = ex.getCause().getMessage();
            }
            log.warn("[Keep-Alive] {} | ERROR -> GET {} | latency={} ms | tipo={} | detalle={}",
                    timestamp, uri, elapsedMs, ex.getClass().getSimpleName(),
                    (detail != null && !detail.isBlank()) ? detail : "conexion rechazada o sin respuesta");
            return false;
        }
    }

    @PreDestroy
    public synchronized void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        log.info("[Keep-Alive] Detenido limpiamente. Resumen: intentos={}, exitosos={}, fallidos={}",
                totalAttempts.get(), successCount.get(), failureCount.get());
    }

    public boolean isRunning() {
        return running.get();
    }

    public long getTotalAttempts() {
        return totalAttempts.get();
    }

    public long getSuccessCount() {
        return successCount.get();
    }

    public long getFailureCount() {
        return failureCount.get();
    }

    private URI parseValidUri(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(rawUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return null;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return null;
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String nowIso() {
        return OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
