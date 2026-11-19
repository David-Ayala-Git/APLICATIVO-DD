package dd.conexiones.dd_ms_crm.web.dto;

import dd.conexiones.dd_ms_crm.domain.model.enums.Technology;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePlanRequest(
        @NotBlank String name,
        @NotNull @Positive Integer downMbps,
        @NotNull @Positive Integer upMbps,
        @NotNull Technology technology,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price
) {
}
