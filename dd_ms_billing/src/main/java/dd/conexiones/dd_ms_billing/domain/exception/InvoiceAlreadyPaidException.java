package dd.conexiones.dd_ms_billing.domain.exception;

public class InvoiceAlreadyPaidException extends RuntimeException {
    public InvoiceAlreadyPaidException(String invoiceId) {
        super("La factura ya esta pagada: " + invoiceId);
    }
}
