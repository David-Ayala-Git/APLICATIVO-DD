package dd.conexiones.dd_ms_auth.web.controller;

import dd.conexiones.dd_ms_auth.application.service.AuthService;
import dd.conexiones.dd_ms_auth.web.dto.LoginRequest;
import dd.conexiones.dd_ms_auth.web.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request, clientIp(http));
    }

    /**
     * X-Client-IP la fija Caddy (infra/caddy/Caddyfile) reemplazando la que mande el
     * navegador; nginx y el gateway la pasan intacta. No sirve X-Forwarded-For: el
     * gateway la descarta y deja solo su propia IP, con lo que todos los logins
     * compartirian el limite por IP. Sin la cabecera (desarrollo) se usa el socket.
     */
    private static String clientIp(HttpServletRequest http) {
        String clientIp = http.getHeader("X-Client-IP");
        if (clientIp != null && !clientIp.isBlank()) {
            return clientIp.trim();
        }
        return http.getRemoteAddr();
    }
}
