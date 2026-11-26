package dd.conexiones.dd_monolith.keepalive;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeepAliveServiceTest {

    private KeepAliveService service;
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (service != null) {
            service.stop();
        }
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void doesNotStartWhenDisabled() throws Exception {
        KeepAliveProperties props = new KeepAliveProperties();
        props.setEnabled(false);
        props.setUrl("http://127.0.0.1:9999/health");
        props.setIntervalMs(80);

        service = new KeepAliveService(props);

        assertFalse(service.start());
        assertFalse(service.isRunning());
        Thread.sleep(200);
        assertEquals(0, service.getTotalAttempts());
    }

    @Test
    void rejectsBlankOrInvalidUrlWithoutStarting() {
        KeepAliveProperties props = new KeepAliveProperties();
        props.setEnabled(true);
        props.setUrl("   ");
        props.setIntervalMs(100);

        service = new KeepAliveService(props);

        assertFalse(service.start());
        assertFalse(service.isRunning());
    }

    @Test
    void startsWithoutDuplicatesAndExecutesPeriodically() throws Exception {
        CountDownLatch hitsLatch = new CountDownLatch(3);
        AtomicInteger requestsReceived = new AtomicInteger(0);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/health", exchange -> {
            requestsReceived.incrementAndGet();
            byte[] payload = "{\"status\":\"ok\",\"message\":\"Backend is running\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(payload);
            }
            hitsLatch.countDown();
        });
        server.start();

        int port = server.getAddress().getPort();
        KeepAliveProperties props = new KeepAliveProperties();
        props.setEnabled(true);
        props.setUrl("http://127.0.0.1:" + port + "/health");
        props.setIntervalMs(100);
        props.setTimeoutMs(2000);

        service = new KeepAliveService(props);

        assertTrue(service.start(), "Primera llamada a start() debe iniciar el temporizador");
        assertFalse(service.start(), "Segunda llamada a start() no debe duplicar el temporizador");
        assertTrue(service.isRunning());

        assertTrue(hitsLatch.await(5, TimeUnit.SECONDS), "Debe ejecutar al menos 3 solicitudes periodicas");
        waitUntil(() -> service.getSuccessCount() >= 3, 2000);
        assertTrue(requestsReceived.get() >= 3);
        assertTrue(service.getSuccessCount() >= 3);
        assertEquals(0, service.getFailureCount());

        service.stop();
        assertFalse(service.isRunning());
    }

    @Test
    void survivesConnectionErrorAndRecoversWhenServerComesUp() throws Exception {
        int unusedPort = findFreePort();

        KeepAliveProperties props = new KeepAliveProperties();
        props.setEnabled(true);
        props.setUrl("http://127.0.0.1:" + unusedPort + "/health");
        props.setIntervalMs(120);
        props.setTimeoutMs(500);

        service = new KeepAliveService(props);
        assertTrue(service.start());

        // Esperar al menos 2 errores reales de conexion (puerto cerrado)
        waitUntil(() -> service.getFailureCount() >= 2, 5000);
        assertTrue(service.isRunning(), "El temporizador debe seguir activo tras errores de conexion");
        assertEquals(0, service.getSuccessCount());

        // Levantar ahora el servidor en el mismo puerto y verificar recuperacion automatica
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", unusedPort), 0);
        server.createContext("/health", exchange -> {
            byte[] payload = "{\"status\":\"ok\",\"message\":\"Backend is running\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(payload);
            }
        });
        server.start();

        waitUntil(() -> service.getSuccessCount() >= 1, 5000);
        assertTrue(service.getSuccessCount() >= 1, "Debe recuperarse y registrar exito en ejecuciones posteriores");
        assertTrue(service.isRunning());
    }

    @Test
    void survivesHttp500AndContinuesNextExecutions() throws Exception {
        AtomicInteger calls = new AtomicInteger(0);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/health", exchange -> {
            int attempt = calls.incrementAndGet();
            int code = attempt <= 2 ? 500 : 200;
            byte[] payload = (code == 200
                    ? "{\"status\":\"ok\",\"message\":\"Backend is running\"}"
                    : "{\"error\":\"simulated\"}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, payload.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(payload);
            }
        });
        server.start();

        KeepAliveProperties props = new KeepAliveProperties();
        props.setEnabled(true);
        props.setUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/health");
        props.setIntervalMs(100);
        props.setTimeoutMs(2000);

        service = new KeepAliveService(props);
        assertTrue(service.start());

        waitUntil(() -> service.getFailureCount() >= 2 && service.getSuccessCount() >= 1, 5000);
        assertTrue(service.getFailureCount() >= 2);
        assertTrue(service.getSuccessCount() >= 1);
        assertTrue(service.isRunning());
    }

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void waitUntil(java.util.function.BooleanSupplier condition, long maxWaitMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + maxWaitMs;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(40);
        }
    }
}
