package dd.conexiones.dd_ms_support.web.dto;

import dd.conexiones.dd_ms_support.domain.model.enums.WorkOrderType;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateWorkOrderRequest(
        @NotNull UUID clientId,
        UUID ticketId,
        @NotNull WorkOrderType type,
        UUID technicianId,
        OffsetDateTime scheduledAt,
        String address
) {
}
