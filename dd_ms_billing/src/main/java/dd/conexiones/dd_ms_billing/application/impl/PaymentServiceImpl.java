package dd.conexiones.dd_ms_billing.application.impl;

import dd.conexiones.dd_ms_billing.application.mapper.PaymentMapper;
import dd.conexiones.dd_ms_billing.application.port.GatewayPaymentEvent;
import dd.conexiones.dd_ms_billing.application.port.PaymentGateway;
import dd.conexiones.dd_ms_billing.application.service.PaymentService;
import dd.conexiones.dd_ms_billing.application.validation.InvoiceValidator;
import dd.conexiones.dd_ms_billing.domain.exception.InvalidPaymentException;
import dd.conexiones.dd_ms_billing.domain.exception.InvoiceNotFoundException;
import dd.conexiones.dd_ms_billing.domain.exception.PaymentNotFoundException;
import dd.conexiones.dd_ms_billing.domain.model.Invoice;
import dd.conexiones.dd_ms_billing.domain.model.Payment;
import dd.conexiones.dd_ms_billing.domain.model.enums.InvoiceStatus;
import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentMethod;
import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentStatus;
import dd.conexiones.dd_ms_billing.domain.repository.InvoiceRepository;
import dd.conexiones.dd_ms_billing.domain.repository.PaymentRepository;
import dd.conexiones.dd_ms_billing.web.dto.ManualPaymentRequest;
import dd.conexiones.dd_ms_billing.web.dto.PaymentDTO;
import dd.conexiones.mscommon.audit.Audited;
import dd.conexiones.mscommon.automation.AutomationEvents;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    /** Wompi y PSE solo entran por su webhook firmado, nunca a mano. */
    private static final Set<PaymentMethod> MANUAL_METHODS = EnumSet.of(PaymentMethod.MANUAL, PaymentMethod.NEQUI);

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceValidator invoiceValidator;
    private final PaymentMapper paymentMapper;
    private final TenantFilterEnabler tenantFilterEnabler;
    private final PaymentGateway paymentGateway;
    private final AutomationEvents automationEvents;

    @PersistenceContext
    private EntityManager entityManager;

    public PaymentServiceImpl(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository,
                               InvoiceValidator invoiceValidator, PaymentMapper paymentMapper,
                               TenantFilterEnabler tenantFilterEnabler, PaymentGateway paymentGateway,
                               AutomationEvents automationEvents) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceValidator = invoiceValidator;
        this.paymentMapper = paymentMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
        this.paymentGateway = paymentGateway;
        this.automationEvents = automationEvents;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return paymentRepository.findAll().stream().map(paymentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        // el filtro de Hibernate no aplica a una carga por id: se compara a mano
        return paymentRepository.findById(id)
                .filter(payment -> payment.getTenantId().equals(TenantContext.require()))
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new PaymentNotFoundException(id.toString()));
    }

    @Override
    @Audited("payment.manual")
    public PaymentDTO registerManual(ManualPaymentRequest request) {
        if (!MANUAL_METHODS.contains(request.method())) {
            throw new InvalidPaymentException("Un pago " + request.method()
                    + " solo se registra por el webhook firmado de la pasarela");
        }
        String tenantId = TenantContext.require();
        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .filter(found -> found.getTenantId().equals(tenantId))
                .orElseThrow(() -> new InvoiceNotFoundException(request.invoiceId().toString()));
        return record(invoice, request.amount(), request.method(), request.externalReference(), true);
    }

    @Override
    public PaymentDTO processGatewayWebhook(String rawBody, Map<String, String> headers) {
        // Primero la firma: sin ella cualquiera en internet marcaria facturas como pagadas.
        GatewayPaymentEvent event = paymentGateway.verifyWebhook(rawBody, headers);
        // La pasarela no conoce nuestro tenant: sale de la propia factura (carga
        // sin filtro a proposito), confiable solo porque la firma ya se verifico.
        Invoice invoice = invoiceRepository.findById(event.invoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException(event.invoiceId().toString()));
        return record(invoice, event.amount(), event.method(), event.externalReference(), event.approved());
    }

    private PaymentDTO record(Invoice invoice, BigDecimal amount, PaymentMethod method,
                              String externalReference, boolean approved) {
        // Reintento del mismo pago (las pasarelas reenvian webhooks): se devuelve
        // el ya registrado en vez de fallar o duplicarlo.
        var existing = paymentRepository.findByTenantIdAndMethodAndExternalReference(
                invoice.getTenantId(), method, externalReference);
        if (existing.isPresent()) {
            return paymentMapper.toDto(existing.get());
        }

        invoiceValidator.validateNotPaid(invoice);
        // Sin pagos parciales: la factura queda PAGADA, asi que el monto debe ser el total.
        if (approved && amount.compareTo(invoice.getAmount()) != 0) {
            throw new InvalidPaymentException("El monto " + amount.toPlainString()
                    + " no coincide con el total de la factura (" + invoice.getAmount().toPlainString() + ")");
        }

        Payment payment = new Payment();
        payment.setTenantId(invoice.getTenantId());
        payment.setInvoiceId(invoice.getId());
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setExternalReference(externalReference);
        if (approved) {
            payment.setStatus(PaymentStatus.CONFIRMED);
            payment.setPaidAt(OffsetDateTime.now());
            invoice.setStatus(InvoiceStatus.PAID);
            invoiceRepository.save(invoice);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        payment = paymentRepository.saveAndFlush(payment);
        if (approved) {
            // n8n (flujo de reactivacion) reconecta al cliente si estaba cortado.
            // tenantId explicito: en el webhook de la pasarela no hay tenant de sesion.
            automationEvents.publish("payment-confirmed", Map.of(
                    "tenantId", invoice.getTenantId(),
                    "invoiceId", invoice.getId().toString(),
                    "clientId", invoice.getClientId().toString(),
                    "paymentId", payment.getId().toString(),
                    "amount", amount,
                    "method", method.name()));
        }
        return paymentMapper.toDto(payment);
    }
}
