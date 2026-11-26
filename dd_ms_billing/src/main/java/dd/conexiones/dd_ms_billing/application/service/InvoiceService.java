package dd.conexiones.dd_ms_billing.application.service;

import dd.conexiones.dd_ms_billing.web.dto.BillingRunResult;
import dd.conexiones.dd_ms_billing.web.dto.CreateInvoiceRequest;
import dd.conexiones.dd_ms_billing.web.dto.InvoiceDTO;
import dd.conexiones.dd_ms_billing.web.dto.UpdateInvoiceRequest;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    List<InvoiceDTO> findAll();
    InvoiceDTO findById(UUID id);
    InvoiceDTO create(CreateInvoiceRequest request);
    InvoiceDTO update(UUID id, UpdateInvoiceRequest request);
    void delete(UUID id);
    BillingRunResult runBilling();
    List<InvoiceDTO> refreshOverdue();
}
