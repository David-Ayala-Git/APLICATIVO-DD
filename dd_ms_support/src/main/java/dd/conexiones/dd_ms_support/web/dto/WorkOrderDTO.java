package dd.conexiones.dd_ms_support.web.dto;

import dd.conexiones.dd_ms_support.domain.model.enums.WorkOrderStatus;
import dd.conexiones.dd_ms_support.domain.model.enums.WorkOrderType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkOrderDTO(
        UUID id,
        UUID clientId,
        UUID ticketId,
        WorkOrderType type,
        UUID technicianId,
        OffsetDateTime scheduledAt,
        String address,
        WorkOrderStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        OffsetDateTime createdAt
) {
}
