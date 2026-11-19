package dd.conexiones.dd_ms_billing.application.impl;

import dd.conexiones.dd_ms_billing.application.mapper.InvoiceMapper;
import dd.conexiones.dd_ms_billing.application.port.ElectronicInvoiceProvider;
import dd.conexiones.dd_ms_billing.application.port.ElectronicInvoiceResult;
import dd.conexiones.dd_ms_billing.application.service.InvoiceService;
import dd.conexiones.dd_ms_billing.domain.exception.InvoiceNotFoundException;
import dd.conexiones.dd_ms_billing.domain.model.Invoice;
import dd.conexiones.dd_ms_billing.domain.model.enums.InvoiceStatus;
import dd.conexiones.dd_ms_billing.domain.repository.InvoiceRepository;
import dd.conexiones.dd_ms_billing.infrastructure.client.CrmClient;
import dd.conexiones.dd_ms_billing.infrastructure.client.dto.ClientSummary;
import dd.conexiones.dd_ms_billing.infrastructure.client.dto.PlanSummary;
import dd.conexiones.mscommon.audit.Audited;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_billing.web.dto.BillingRunResult;
import dd.conexiones.dd_ms_billing.web.dto.CreateInvoiceRequest;
import dd.conexiones.dd_ms_billing.web.dto.InvoiceDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final TenantFilterEnabler tenantFilterEnabler;
    private final CrmClient crmClient;
    private final ElectronicInvoiceProvider electronicInvoiceProvider;

    @PersistenceContext
    private EntityManager entityManager;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper,
                               TenantFilterEnabler tenantFilterEnabler, CrmClient crmClient,
                               ElectronicInvoiceProvider electronicInvoiceProvider) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
        this.crmClient = crmClient;
        this.electronicInvoiceProvider = electronicInvoiceProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return invoiceRepository.findAll().stream().map(invoiceMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        // el filtro de Hibernate no aplica a una carga por id: se compara a mano
        return invoiceRepository.findById(id)
                .filter(invoice -> invoice.getTenantId().equals(TenantContext.require()))
                .map(invoiceMapper::toDto)
                .orElseThrow(() -> new InvoiceNotFoundException(id.toString()));
    }

    @Override
    public InvoiceDTO create(CreateInvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setTenantId(TenantContext.require());
        invoice.setClientId(request.clientId());
        invoice.setPlanId(request.planId());
        invoice.setPeriod(request.period());
        invoice.setAmount(request.amount());
        invoice.setDueDate(request.dueDate());

        invoice = invoiceRepository.saveAndFlush(invoice);
        issueElectronically(invoice);
        return invoiceMapper.toDto(invoice);
    }

    @Override
    public BillingRunResult runBilling() {
        String tenantId = TenantContext.require();
        String period = YearMonth.now().format(PERIOD_FORMAT);
        LocalDate dueDate = LocalDate.now().plusDays(10);

        List<ClientSummary> activeClients = crmClient.findActiveClients();

        int created = 0;
        int skipped = 0;

        for (ClientSummary client : activeClients) {
            if (invoiceRepository.existsByTenantIdAndClientIdAndPeriod(tenantId, client.id(), period)) {
                skipped++;
                continue;
            }

            PlanSummary plan = crmClient.findPlan(client.planId());
            if (plan == null) {
                skipped++;
                continue;
            }

            Invoice invoice = new Invoice();
            invoice.setTenantId(tenantId);
            invoice.setClientId(client.id());
            invoice.setPlanId(plan.id());
            invoice.setPeriod(period);
            invoice.setAmount(plan.price());
            invoice.setDueDate(dueDate);
            invoice.setStatus(InvoiceStatus.PENDING);
            issueElectronically(invoiceRepository.save(invoice));
            created++;
        }

        return new BillingRunResult(period, activeClients.size(), created, skipped);
    }

    @Override
    @Audited("billing.overdue_refresh")
    public List<InvoiceDTO> refreshOverdue() {
        // Lo dispara n8n cada dia (flujo de corte por mora): primero marca, luego
        // devuelve TODAS las vencidas del tenant, no solo las recien marcadas, para
        // que un corte que fallo ayer se reintente hoy.
        String tenantId = TenantContext.require();
        invoiceRepository.markOverdue(tenantId, LocalDate.now());
        return invoiceRepository.findByTenantIdAndStatus(tenantId, InvoiceStatus.OVERDUE).stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    /**
     * Emite ante la DIAN via el proveedor configurado. Sin proveedor la factura
     * queda NOT_ISSUED: existe y se puede cobrar, pero no tiene CUFE.
     */
    private void issueElectronically(Invoice invoice) {
        ElectronicInvoiceResult result = electronicInvoiceProvider.issue(invoice);
        invoice.setEinvoiceStatus(result.status());
        invoice.setCufe(result.cufe());
        invoice.setPdfUrl(result.pdfUrl());
        invoice.setXmlUrl(result.xmlUrl());
        invoice.setEinvoiceError(result.error());
    }
}
