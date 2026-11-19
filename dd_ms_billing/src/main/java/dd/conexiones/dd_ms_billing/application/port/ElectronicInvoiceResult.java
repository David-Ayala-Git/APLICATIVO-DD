package dd.conexiones.dd_ms_billing.application.port;

import dd.conexiones.dd_ms_billing.domain.model.enums.ElectronicInvoiceStatus;

public record ElectronicInvoiceResult(
        ElectronicInvoiceStatus status,
        String cufe,
        String pdfUrl,
        String xmlUrl,
        String error
) {
    public static ElectronicInvoiceResult notIssued() {
        return new ElectronicInvoiceResult(ElectronicInvoiceStatus.NOT_ISSUED, null, null, null, null);
    }
}
