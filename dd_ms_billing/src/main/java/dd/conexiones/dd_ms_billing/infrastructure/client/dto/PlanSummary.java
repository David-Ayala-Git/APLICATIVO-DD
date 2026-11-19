package dd.conexiones.dd_ms_billing.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanSummary(
        UUID id,
        String name,
        BigDecimal price
) {
}
