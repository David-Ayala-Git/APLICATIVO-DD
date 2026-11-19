package dd.conexiones.dd_ms_billing.application.service;

import dd.conexiones.dd_ms_billing.web.dto.BillingRunResult;
import dd.conexiones.dd_ms_billing.web.dto.CreateInvoiceRequest;
import dd.conexiones.dd_ms_billing.web.dto.InvoiceDTO;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    List<InvoiceDTO> findAll();
    InvoiceDTO findById(UUID id);
    InvoiceDTO create(CreateInvoiceRequest request);
    BillingRunResult runBilling();
    List<InvoiceDTO> refreshOverdue();
}
