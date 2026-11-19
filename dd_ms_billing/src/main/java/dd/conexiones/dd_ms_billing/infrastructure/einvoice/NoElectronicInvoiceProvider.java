package dd.conexiones.dd_ms_billing.infrastructure.einvoice;

import dd.conexiones.dd_ms_billing.application.port.ElectronicInvoiceProvider;
import dd.conexiones.dd_ms_billing.application.port.ElectronicInvoiceResult;
import dd.conexiones.dd_ms_billing.domain.model.Invoice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Sin proveedor DIAN contratado: la factura queda NOT_ISSUED. A proposito no
 * genera un CUFE "de prueba": un CUFE falso en una factura real es peor que
 * ninguno.
 */
@Component
@ConditionalOnProperty(name = "billing.einvoice.provider", havingValue = "none", matchIfMissing = true)
public class NoElectronicInvoiceProvider implements ElectronicInvoiceProvider {

    @Override
    public String key() {
        return "none";
    }

    @Override
    public ElectronicInvoiceResult issue(Invoice invoice) {
        return ElectronicInvoiceResult.notIssued();
    }
}
