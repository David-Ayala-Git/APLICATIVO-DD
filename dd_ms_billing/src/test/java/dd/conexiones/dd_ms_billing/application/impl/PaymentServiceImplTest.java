package dd.conexiones.dd_ms_billing.application.impl;

import dd.conexiones.dd_ms_billing.application.mapper.PaymentMapper;
import dd.conexiones.dd_ms_billing.application.port.GatewayPaymentEvent;
import dd.conexiones.dd_ms_billing.application.port.PaymentGateway;
import dd.conexiones.dd_ms_billing.application.validation.InvoiceValidator;
import dd.conexiones.dd_ms_billing.domain.exception.InvalidPaymentException;
import dd.conexiones.dd_ms_billing.domain.exception.InvoiceAlreadyPaidException;
import dd.conexiones.dd_ms_billing.domain.exception.InvoiceNotFoundException;
import dd.conexiones.dd_ms_billing.domain.exception.PaymentGatewayNotConfiguredException;
import dd.conexiones.dd_ms_billing.domain.model.Invoice;
import dd.conexiones.dd_ms_billing.domain.model.Payment;
import dd.conexiones.dd_ms_billing.domain.model.enums.InvoiceStatus;
import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentMethod;
import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentStatus;
import dd.conexiones.dd_ms_billing.domain.repository.InvoiceRepository;
import dd.conexiones.dd_ms_billing.domain.repository.PaymentRepository;
import dd.conexiones.dd_ms_billing.web.dto.ManualPaymentRequest;
import dd.conexiones.dd_ms_billing.web.dto.PaymentDTO;
import dd.conexiones.mscommon.automation.AutomationEvents;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de cobro: nada de esto pasa por la base de datos (repositorios simulados). */
class PaymentServiceImplTest {

    private static final String ISP = "isp-a";
    private static final BigDecimal TOTAL = new BigDecimal("120000.00");

    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final InvoiceRepository invoices = mock(InvoiceRepository.class);
    private final PaymentGateway gateway = mock(PaymentGateway.class);
    private final AutomationEvents events = mock(AutomationEvents.class);

    private final PaymentServiceImpl service = new PaymentServiceImpl(payments, invoices, new InvoiceValidator(),
            new PaymentMapper(), mock(TenantFilterEnabler.class), gateway, events);

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        TenantContext.set(ISP);
        invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setTenantId(ISP);
        invoice.setClientId(UUID.randomUUID());
        invoice.setAmount(TOTAL);
        when(invoices.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(payments.findByTenantIdAndMethodAndExternalReference(anyString(), any(), anyString()))
                .thenReturn(Optional.empty());
        when(payments.saveAndFlush(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    private ManualPaymentRequest manual(BigDecimal amount, PaymentMethod method) {
        return new ManualPaymentRequest(invoice.getId(), amount, method, "RECIBO-1");
    }

    @Test
    void pagoManualCorrectoMarcaLaFacturaPagadaYAvisaAN8n() {
        PaymentDTO dto = service.registerManual(manual(TOTAL, PaymentMethod.MANUAL));

        assertThat(dto.status()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        verify(events).publish(eq("payment-confirmed"), anyMap());
    }

    @Test
    void montoDistintoAlTotalSeRechazaSinTocarLaFactura() {
        assertThatThrownBy(() -> service.registerManual(manual(new BigDecimal("1000"), PaymentMethod.MANUAL)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessageContaining("no coincide");
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        verify(payments, never()).saveAndFlush(any());
    }

    @Test
    void montoConOtraEscalaPeroIgualSeAcepta() {
        // 120000 y 120000.00 son el mismo valor: compareTo, no equals
        service.registerManual(manual(new BigDecimal("120000"), PaymentMethod.NEQUI));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void wompiNoSeRegistraAMano() {
        assertThatThrownBy(() -> service.registerManual(manual(TOTAL, PaymentMethod.WOMPI)))
                .isInstanceOf(InvalidPaymentException.class);
        verify(invoices, never()).findById(any());
    }

    @Test
    void facturaDeOtroIspNoExisteParaQuienLlama() {
        invoice.setTenantId("isp-b");
        assertThatThrownBy(() -> service.registerManual(manual(TOTAL, PaymentMethod.MANUAL)))
                .isInstanceOf(InvoiceNotFoundException.class);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
    }

    @Test
    void facturaYaPagadaNoSePagaDosVeces() {
        invoice.setStatus(InvoiceStatus.PAID);
        assertThatThrownBy(() -> service.registerManual(manual(TOTAL, PaymentMethod.MANUAL)))
                .isInstanceOf(InvoiceAlreadyPaidException.class);
    }

    @Test
    void mismoComprobanteDevuelveElPagoExistente() {
        Payment existing = new Payment();
        existing.setId(UUID.randomUUID());
        existing.setStatus(PaymentStatus.CONFIRMED);
        when(payments.findByTenantIdAndMethodAndExternalReference(ISP, PaymentMethod.MANUAL, "RECIBO-1"))
                .thenReturn(Optional.of(existing));

        PaymentDTO dto = service.registerManual(manual(TOTAL, PaymentMethod.MANUAL));

        assertThat(dto.id()).isEqualTo(existing.getId());
        verify(payments, never()).saveAndFlush(any());
        verify(events, never()).publish(anyString(), anyMap());
    }

    @Test
    void webhookSinPasarelaNoTocaNada() {
        when(gateway.verifyWebhook(anyString(), anyMap())).thenThrow(new PaymentGatewayNotConfiguredException());

        assertThatThrownBy(() -> service.processGatewayWebhook("{}", Map.of()))
                .isInstanceOf(PaymentGatewayNotConfiguredException.class);
        verify(invoices, never()).findById(any());
        verify(payments, never()).saveAndFlush(any());
    }

    @Test
    void webhookRechazadoQuedaFallidoYNoReactiva() {
        TenantContext.clear(); // la pasarela no trae tenant
        when(gateway.verifyWebhook(anyString(), anyMap())).thenReturn(
                new GatewayPaymentEvent(invoice.getId(), TOTAL, PaymentMethod.WOMPI, "TX-9", false));

        PaymentDTO dto = service.processGatewayWebhook("{}", Map.of());

        assertThat(dto.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        verify(events, never()).publish(anyString(), anyMap());
    }
}
