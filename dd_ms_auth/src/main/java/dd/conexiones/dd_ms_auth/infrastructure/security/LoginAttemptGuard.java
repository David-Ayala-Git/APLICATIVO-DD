package dd.conexiones.dd_ms_auth.infrastructure.security;

import dd.conexiones.dd_ms_auth.domain.exception.TooManyLoginAttemptsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freno a la fuerza bruta sobre /api/auth/login, en memoria (auth corre en una
 * sola instancia). Cuenta solo intentos fallidos en una ventana de 15 minutos:
 * <ul>
 *   <li>correo + IP: 5 fallos. Es el limite que siente quien olvido su clave.</li>
 *   <li>IP: 20 fallos, para quien prueba muchos correos desde un mismo origen.</li>
 *   <li>correo: 50 fallos desde cualquier IP, para ataques distribuidos. Es alto a
 *       proposito: un tercero podria usarlo para bloquear la cuenta de otro.</li>
 * </ul>
 * Al superar un limite la llave queda bloqueada hasta que vence su ventana. Un
 * ingreso correcto limpia la llave correo + IP. Si auth se reinicia los contadores
 * vuelven a cero, aceptable para este volumen.
 */
@Component
public class LoginAttemptGuard {

    static final Duration WINDOW = Duration.ofMinutes(15);
    static final int MAX_PER_ACCOUNT_AND_IP = 5;
    static final int MAX_PER_IP = 20;
    static final int MAX_PER_ACCOUNT = 50;

    /** Por encima de esto se purgan las ventanas vencidas antes de seguir creciendo. */
    private static final int PRUNE_THRESHOLD = 10_000;

    private record Window(Instant start, int failures) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    @Autowired
    public LoginAttemptGuard() {
        this(Clock.systemUTC());
    }

    LoginAttemptGuard(Clock clock) {
        this.clock = clock;
    }

    /** Lanza {@link TooManyLoginAttemptsException} si alguna de las tres llaves esta agotada. */
    public void checkAllowed(String email, String ip) {
        Instant now = clock.instant();
        long retryAfter = Math.max(
                Math.max(retryAfter(accountIpKey(email, ip), MAX_PER_ACCOUNT_AND_IP, now),
                        retryAfter(ipKey(ip), MAX_PER_IP, now)),
                retryAfter(accountKey(email), MAX_PER_ACCOUNT, now));
        if (retryAfter > 0) {
            throw new TooManyLoginAttemptsException(retryAfter);
        }
    }

    public void recordFailure(String email, String ip) {
        Instant now = clock.instant();
        if (windows.size() > PRUNE_THRESHOLD) {
            windows.values().removeIf(w -> expired(w, now));
        }
        for (String key : new String[]{accountIpKey(email, ip), ipKey(ip), accountKey(email)}) {
            windows.compute(key, (k, w) -> w == null || expired(w, now)
                    ? new Window(now, 1)
                    : new Window(w.start(), w.failures() + 1));
        }
    }

    public void recordSuccess(String email, String ip) {
        windows.remove(accountIpKey(email, ip));
    }

    private long retryAfter(String key, int max, Instant now) {
        Window w = windows.get(key);
        if (w == null || expired(w, now) || w.failures() < max) {
            return 0;
        }
        return Math.max(1, Duration.between(now, w.start().plus(WINDOW)).toSeconds());
    }

    private static boolean expired(Window w, Instant now) {
        return !now.isBefore(w.start().plus(WINDOW));
    }

    private static String accountIpKey(String email, String ip) {
        return "a+i:" + normalize(email) + "|" + ip;
    }

    private static String accountKey(String email) {
        return "a:" + normalize(email);
    }

    private static String ipKey(String ip) {
        return "i:" + ip;
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
