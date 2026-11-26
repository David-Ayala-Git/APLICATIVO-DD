package dd.conexiones.dd_ms_billing.web.dto;

import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentMethod;
import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdatePaymentRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull PaymentMethod method,
        @NotNull PaymentStatus status,
        @NotBlank String externalReference
) {
}
