package dd.conexiones.dd_ms_billing.application.port;

import dd.conexiones.dd_ms_billing.domain.model.Invoice;

/**
 * Proveedor tecnologico autorizado por la DIAN (Alegra, Siigo, The Factory HKA...).
 * Todavia no hay uno contratado: rige NoElectronicInvoiceProvider. Para conectar
 * uno real, se implementa esta interfaz en infrastructure/einvoice con
 * {@code @ConditionalOnProperty(name = "billing.einvoice.provider", havingValue = "<clave>")}
 * y se define esa clave en la configuracion; nada mas del servicio cambia.
 */
public interface ElectronicInvoiceProvider {

    /** Clave de configuracion del proveedor ("none" mientras no haya uno). */
    String key();

    /**
     * Emite la factura ante la DIAN. No lanza por rechazos de negocio: los
     * devuelve como REJECTED con el motivo, para no tumbar una corrida masiva
     * por una sola factura.
     */
    ElectronicInvoiceResult issue(Invoice invoice);
}
