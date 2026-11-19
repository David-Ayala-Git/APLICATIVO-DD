package dd.conexiones.dd_ms_billing.infrastructure.client.dto;

import java.util.UUID;

public record ClientSummary(
        UUID id,
        String fullName,
        UUID planId,
        String status
) {
}
