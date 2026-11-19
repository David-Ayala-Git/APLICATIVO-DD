package dd.conexiones.dd_ms_auth.infrastructure.security;

import dd.conexiones.dd_ms_auth.domain.exception.TooManyLoginAttemptsException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptGuardTest {

    /** Reloj que el test adelanta a mano. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T10:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MutableClock clock = new MutableClock();
    private final LoginAttemptGuard guard = new LoginAttemptGuard(clock);

    private void fail(String email, String ip, int times) {
        for (int i = 0; i < times; i++) {
            guard.recordFailure(email, ip);
        }
    }

    @Test
    void bloqueaCorreoEIpAlQuintoFallo() {
        fail("a@isp.co", "1.1.1.1", 4);
        assertThatCode(() -> guard.checkAllowed("a@isp.co", "1.1.1.1")).doesNotThrowAnyException();

        fail("a@isp.co", "1.1.1.1", 1);
        assertThatThrownBy(() -> guard.checkAllowed("A@ISP.co ", "1.1.1.1"))
                .isInstanceOf(TooManyLoginAttemptsException.class)
                .satisfies(e -> assertThat(((TooManyLoginAttemptsException) e).getRetryAfterSeconds())
                        .isEqualTo(LoginAttemptGuard.WINDOW.toSeconds()));
    }

    @Test
    void elBloqueoDeUnaIpNoAfectaAlMismoCorreoDesdeOtra() {
        fail("a@isp.co", "1.1.1.1", 5);
        assertThatCode(() -> guard.checkAllowed("a@isp.co", "2.2.2.2")).doesNotThrowAnyException();
    }

    @Test
    void seLiberaAlVencerLaVentana() {
        fail("a@isp.co", "1.1.1.1", 5);
        clock.advance(LoginAttemptGuard.WINDOW);
        assertThatCode(() -> guard.checkAllowed("a@isp.co", "1.1.1.1")).doesNotThrowAnyException();
    }

    @Test
    void unaIpQuePruebaMuchosCorreosQuedaBloqueada() {
        for (int i = 0; i < LoginAttemptGuard.MAX_PER_IP; i++) {
            guard.recordFailure("u" + i + "@isp.co", "3.3.3.3");
        }
        assertThatThrownBy(() -> guard.checkAllowed("nuevo@isp.co", "3.3.3.3"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    @Test
    void ataqueDistribuidoSobreUnCorreoQuedaBloqueado() {
        for (int i = 0; i < LoginAttemptGuard.MAX_PER_ACCOUNT; i++) {
            guard.recordFailure("victima@isp.co", "10.0.0." + i);
        }
        assertThatThrownBy(() -> guard.checkAllowed("victima@isp.co", "10.9.9.9"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    @Test
    void ingresoCorrectoLimpiaLosFallosDeEseCorreoEIp() {
        fail("a@isp.co", "1.1.1.1", 4);
        guard.recordSuccess("a@isp.co", "1.1.1.1");
        fail("a@isp.co", "1.1.1.1", 4);
        assertThatCode(() -> guard.checkAllowed("a@isp.co", "1.1.1.1")).doesNotThrowAnyException();
    }
}
