package dd.conexiones.dd_ms_billing.application.mapper;

import dd.conexiones.dd_ms_billing.domain.model.Invoice;
import dd.conexiones.dd_ms_billing.web.dto.InvoiceDTO;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceDTO toDto(Invoice invoice) {
        return new InvoiceDTO(invoice.getId(), invoice.getClientId(), invoice.getPlanId(), invoice.getPeriod(),
                invoice.getAmount(), invoice.getStatus(), invoice.getDueDate(), invoice.getIssuedAt(),
                invoice.getCufe(), invoice.getPdfUrl(), invoice.getXmlUrl(),
                invoice.getEinvoiceStatus(), invoice.getEinvoiceError());
    }
}
