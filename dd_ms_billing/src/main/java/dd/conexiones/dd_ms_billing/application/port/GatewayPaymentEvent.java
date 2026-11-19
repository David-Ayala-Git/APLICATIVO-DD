package dd.conexiones.dd_ms_billing.application.port;

import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

/** Transaccion ya verificada por la pasarela, en terminos de nuestra factura. */
public record GatewayPaymentEvent(
        UUID invoiceId,
        BigDecimal amount,
        PaymentMethod method,
        String externalReference,
        boolean approved
) {
}
