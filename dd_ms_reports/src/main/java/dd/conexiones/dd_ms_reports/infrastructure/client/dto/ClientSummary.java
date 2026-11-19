package dd.conexiones.dd_ms_reports.infrastructure.client.dto;

import java.util.UUID;

public record ClientSummary(
        UUID id,
        String status,
        UUID planId
) {
}
