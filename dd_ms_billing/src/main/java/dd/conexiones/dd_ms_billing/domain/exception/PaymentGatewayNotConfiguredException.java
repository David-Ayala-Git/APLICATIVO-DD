package dd.conexiones.dd_ms_billing.domain.exception;

public class PaymentGatewayNotConfiguredException extends RuntimeException {
    public PaymentGatewayNotConfiguredException() {
        super("No hay pasarela de pagos configurada: este webhook no acepta pagos todavia");
    }
}
