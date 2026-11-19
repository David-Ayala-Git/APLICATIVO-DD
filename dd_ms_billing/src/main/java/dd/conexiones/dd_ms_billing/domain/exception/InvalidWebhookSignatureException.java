package dd.conexiones.dd_ms_billing.domain.exception;

/** La firma del webhook no corresponde al secreto de la pasarela: no se registra nada. */
public class InvalidWebhookSignatureException extends RuntimeException {
    public InvalidWebhookSignatureException(String message) {
        super(message);
    }
}
