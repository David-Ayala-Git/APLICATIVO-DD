package dd.conexiones.dd_ms_billing.domain.exception;

/** Pago que no cuadra con la factura (monto distinto, metodo no permitido...). */
public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
