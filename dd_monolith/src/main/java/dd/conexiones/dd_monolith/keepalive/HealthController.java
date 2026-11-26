package dd.conexiones.dd_monolith.keepalive;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint publico y ligero de salud para verificaciones externas y Keep-Alive.
 * No consulta infraestructura sensible ni expone detalles internos del servidor.
 */
@RestController
public class HealthController {

    private static final Map<String, String> OK_RESPONSE = Map.of(
            "status", "ok",
            "message", "Backend is running"
    );

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(OK_RESPONSE);
    }
}
