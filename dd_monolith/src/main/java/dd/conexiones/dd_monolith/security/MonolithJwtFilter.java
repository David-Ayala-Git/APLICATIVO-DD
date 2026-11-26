package dd.conexiones.dd_monolith.security;

import dd.conexiones.dd_monolith.config.MonolithRestClientConfig;
import dd.conexiones.dd_ms_auth.infrastructure.security.JwtService;
import dd.conexiones.mscommon.role.Roles;
import dd.conexiones.mscommon.tenant.TenantFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Unifica la frontera JWT de dd_ms_gateway y la autenticacion de dd_ms_auth:
 * - Valida el JWT en todas las rutas privadas y responde 401 JSON si falta o es invalido.
 * - Descarta cualquier X-Tenant-Id / X-User-Id / X-User-Role enviado por el cliente y los
 *   inyecta exclusivamente desde los claims firmados del JWT (para TenantFilter).
 * - Permite llamadas internas loopback autenticadas con el token efimero de proceso.
 */
public class MonolithJwtFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/login",
            "/actuator",
            "/api/whatsapp/webhook",
            "/api/payments/webhook"
    );

    private static final Set<String> PUBLIC_EXACT_PATHS = Set.of(
            "/health"
    );

    private static final Set<String> PROTECTED_HEADERS = Set.of(
            TenantFilter.TENANT_HEADER.toLowerCase(Locale.ROOT),
            TenantFilter.USER_ID_HEADER.toLowerCase(Locale.ROOT),
            TenantFilter.USER_ROLE_HEADER.toLowerCase(Locale.ROOT),
            MonolithRestClientConfig.INTERNAL_TOKEN_HEADER.toLowerCase(Locale.ROOT)
    );

    private final JwtService jwtService;
    private final String internalToken;

    public MonolithJwtFilter(JwtService jwtService, String internalToken) {
        this.jwtService = jwtService;
        this.internalToken = internalToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Llamada interna loopback originada por el propio RestClient @LoadBalanced del monolito
        String incomingInternalToken = request.getHeader(MonolithRestClientConfig.INTERNAL_TOKEN_HEADER);
        if (incomingInternalToken != null && constantTimeEquals(incomingInternalToken, internalToken)) {
            String role = request.getHeader(TenantFilter.USER_ROLE_HEADER);
            String effectiveRole = (role != null && !role.isBlank()) ? role : Roles.SERVICE;
            var auth = new UsernamePasswordAuthenticationToken(
                    "internal-service",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + effectiveRole))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (isPublic(path)) {
            chain.doFilter(new VerifiedHeadersRequestWrapper(request, Map.of()), response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            reject(response, "Falta el header Authorization: Bearer <token>");
            return;
        }

        try {
            Claims claims = jwtService.parse(authHeader.substring(7));
            String tenantId = claims.get("tenantId", String.class);
            String role = claims.get("role", String.class);
            String userId = claims.getSubject();

            var authorities = role != null
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    : List.<SimpleGrantedAuthority>of();
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Map<String, String> verifiedHeaders = new HashMap<>();
            if (tenantId != null) {
                verifiedHeaders.put(TenantFilter.TENANT_HEADER.toLowerCase(Locale.ROOT), tenantId);
            }
            if (userId != null) {
                verifiedHeaders.put(TenantFilter.USER_ID_HEADER.toLowerCase(Locale.ROOT), userId);
            }
            if (role != null) {
                verifiedHeaders.put(TenantFilter.USER_ROLE_HEADER.toLowerCase(Locale.ROOT), role);
            }

            chain.doFilter(new VerifiedHeadersRequestWrapper(request, verifiedHeaders), response);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            reject(response, "Token invalido o expirado");
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_EXACT_PATHS.contains(path)
                || PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        response.getWriter().write(body);
    }

    /**
     * Reemplaza X-Tenant-Id / X-User-Id / X-User-Role con los claims del JWT y oculta
     * cualquier valor enviado por el cliente en esas cabeceras.
     */
    private static final class VerifiedHeadersRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> overrides;

        VerifiedHeadersRequestWrapper(HttpServletRequest request, Map<String, String> overrides) {
            super(request);
            this.overrides = overrides;
        }

        @Override
        public String getHeader(String name) {
            String lower = name.toLowerCase(Locale.ROOT);
            if (PROTECTED_HEADERS.contains(lower)) {
                return overrides.get(lower);
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String lower = name.toLowerCase(Locale.ROOT);
            if (PROTECTED_HEADERS.contains(lower)) {
                String value = overrides.get(lower);
                return value == null
                        ? Collections.emptyEnumeration()
                        : Collections.enumeration(List.of(value));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                String name = original.nextElement();
                if (!PROTECTED_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                    names.add(name);
                }
            }
            names.addAll(overrides.keySet());
            return Collections.enumeration(names);
        }
    }
}
