package dd.conexiones.dd_ms_billing.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateInvoiceRequest(
        @NotNull UUID clientId,
        UUID planId,
        @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "periodo debe tener formato YYYY-MM") String period,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @NotNull LocalDate dueDate
) {
}
