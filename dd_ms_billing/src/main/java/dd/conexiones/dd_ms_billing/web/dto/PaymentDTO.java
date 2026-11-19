package dd.conexiones.dd_ms_billing.web.dto;

import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentMethod;
import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentDTO(
        UUID id,
        UUID invoiceId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String externalReference,
        OffsetDateTime paidAt
) {
}
