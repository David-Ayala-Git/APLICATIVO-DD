package dd.conexiones.dd_ms_billing.application.validation;

import dd.conexiones.dd_ms_billing.domain.exception.InvoiceAlreadyPaidException;
import dd.conexiones.dd_ms_billing.domain.model.Invoice;
import dd.conexiones.dd_ms_billing.domain.model.enums.InvoiceStatus;
import org.springframework.stereotype.Component;

@Component
public class InvoiceValidator {

    public void validateNotPaid(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidException(invoice.getId().toString());
        }
    }
}
