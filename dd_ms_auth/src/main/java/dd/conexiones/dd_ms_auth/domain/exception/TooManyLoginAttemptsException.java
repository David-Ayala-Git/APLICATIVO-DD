package dd.conexiones.dd_ms_auth.domain.exception;

public class TooManyLoginAttemptsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyLoginAttemptsException(long retryAfterSeconds) {
        super("Demasiados intentos de ingreso fallidos. Intenta de nuevo en "
                + Math.max(1, (retryAfterSeconds + 59) / 60) + " minuto(s).");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
