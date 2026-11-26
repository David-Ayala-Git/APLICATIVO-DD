package dd.conexiones.dd_ms_billing.web.dto;

import dd.conexiones.dd_ms_billing.domain.model.enums.InvoiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateInvoiceRequest(
        UUID planId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDate dueDate,
        InvoiceStatus status
) {
}
