package dd.conexiones.dd_ms_reports.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExecutiveReportDTO(
        String period,
        BigDecimal mrr,
        BigDecimal arpu,
        BigDecimal churnRate,
        int activeClients,
        int retiredClients,
        int totalClients,
        OffsetDateTime generatedAt
) {
}
