package dd.conexiones.dd_ms_reports.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanSummary(
        UUID id,
        BigDecimal price
) {
}
