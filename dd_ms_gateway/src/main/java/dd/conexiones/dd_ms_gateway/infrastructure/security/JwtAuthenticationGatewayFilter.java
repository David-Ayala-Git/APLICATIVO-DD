package dd.conexiones.dd_ms_gateway.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Frontera de seguridad del ecosistema: valida la firma del JWT emitido por
 * ddc_ms_auth y, solo si es valido, fija X-Tenant-Id / X-User-Id / X-User-Role
 * con los valores del token - descartando cualquier valor que haya mandado el
 * cliente. Ningun microservicio de dominio debe confiar en un tenant que no
 * haya pasado por aqui (regla de oro de la guia, seccion 2 y 12).
 */
@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    /** Rutas que no requieren JWT: login, health checks y webhooks de terceros
     *  (Meta / pasarela de pago) que no pueden enviar un Authorization de usuario. */
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/login",
            "/actuator",
            "/api/whatsapp/webhook",
            "/api/payments/webhook"
    );

    private final SecretKey key;

    public JwtAuthenticationGatewayFilter(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // El preflight de CORS nunca lleva Authorization - lo prohibe la spec del
        // navegador -, asi que responderle 401 romperia cualquier front servido
        // desde otro origen antes de que pueda mandar la peticion real.
        if (CorsUtils.isPreFlightRequest(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(exchange, "Falta el header Authorization: Bearer <token>");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authHeader.substring(7))
                    .getPayload();

            String tenantId = claims.get("tenantId", String.class);
            String role = claims.get("role", String.class);
            String userId = claims.getSubject();

            ServerHttpRequest.Builder mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        // nunca confiar en lo que haya mandado el cliente
                        headers.remove("X-Tenant-Id");
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Role");
                        if (tenantId != null) {
                            headers.add("X-Tenant-Id", tenantId);
                        }
                        if (userId != null) {
                            headers.add("X-User-Id", userId);
                        }
                        if (role != null) {
                            headers.add("X-User-Role", role);
                        }
                    });

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest.build()).build();
            return chain.filter(mutatedExchange);
        } catch (JwtException | IllegalArgumentException e) {
            return reject(exchange, "Token invalido o expirado");
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
