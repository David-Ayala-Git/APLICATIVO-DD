package dd.conexiones.dd_ms_billing.domain.model.enums;

/** Estado de la factura ante la DIAN, independiente de si ya se pago (InvoiceStatus). */
public enum ElectronicInvoiceStatus {
    /** Sin emitir: no hay proveedor tecnologico configurado o aun no se envio. */
    NOT_ISSUED,
    /** Aceptada por la DIAN: tiene CUFE, PDF y XML. */
    ISSUED,
    /** El proveedor o la DIAN la rechazaron; el motivo queda en einvoiceError. */
    REJECTED
}
