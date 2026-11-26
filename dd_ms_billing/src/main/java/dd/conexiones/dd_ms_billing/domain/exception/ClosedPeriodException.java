package dd.conexiones.dd_ms_billing.domain.exception;

public class ClosedPeriodException extends RuntimeException {
    public ClosedPeriodException(String message) {
        super(message);
    }
}
