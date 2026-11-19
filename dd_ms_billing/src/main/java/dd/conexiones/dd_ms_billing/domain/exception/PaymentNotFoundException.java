package dd.conexiones.dd_ms_billing.domain.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String identifier) {
        super("Pago no encontrado: " + identifier);
    }
}
