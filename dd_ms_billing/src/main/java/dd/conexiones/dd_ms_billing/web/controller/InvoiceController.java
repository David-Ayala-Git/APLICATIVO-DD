package dd.conexiones.dd_ms_billing.web.controller;

import dd.conexiones.dd_ms_billing.application.service.InvoiceService;
import dd.conexiones.dd_ms_billing.web.dto.BillingRunResult;
import dd.conexiones.dd_ms_billing.web.dto.CreateInvoiceRequest;
import dd.conexiones.dd_ms_billing.web.dto.InvoiceDTO;
import dd.conexiones.dd_ms_billing.web.dto.UpdateInvoiceRequest;
import dd.conexiones.mscommon.module.RequiresModule;
import dd.conexiones.mscommon.role.RequiresRole;
import dd.conexiones.mscommon.role.Roles;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiresModule("billing")
@RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA, Roles.OPERADOR})
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/invoices")
    public List<InvoiceDTO> findAll() {
        return invoiceService.findAll();
    }

    @GetMapping("/invoices/{id}")
    public InvoiceDTO findById(@PathVariable UUID id) {
        return invoiceService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA})
    @PostMapping("/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceDTO create(@Valid @RequestBody CreateInvoiceRequest request) {
        return invoiceService.create(request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA})
    @PutMapping("/invoices/{id}")
    public InvoiceDTO update(@PathVariable UUID id, @Valid @RequestBody UpdateInvoiceRequest request) {
        return invoiceService.update(id, request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA})
    @DeleteMapping("/invoices/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        invoiceService.delete(id);
    }

    /** Marca como vencidas las pendientes con fecha limite pasada y devuelve todas las vencidas. */
    @RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA})
    @PostMapping("/overdue")
    public List<InvoiceDTO> refreshOverdue() {
        return invoiceService.refreshOverdue();
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA})
    @PostMapping("/run")
    public BillingRunResult runBilling() {
        return invoiceService.runBilling();
    }
}
