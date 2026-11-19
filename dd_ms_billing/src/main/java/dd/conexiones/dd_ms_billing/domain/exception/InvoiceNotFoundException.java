package dd.conexiones.dd_ms_billing.domain.exception;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String identifier) {
        super("Factura no encontrada: " + identifier);
    }
}
