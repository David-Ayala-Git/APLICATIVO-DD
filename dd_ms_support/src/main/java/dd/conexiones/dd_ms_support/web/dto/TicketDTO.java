package dd.conexiones.dd_ms_support.web.dto;

import dd.conexiones.dd_ms_support.domain.model.enums.Priority;
import dd.conexiones.dd_ms_support.domain.model.enums.TicketStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketDTO(
        UUID id,
        UUID clientId,
        String reason,
        Priority priority,
        TicketStatus status,
        UUID assignedTo,
        OffsetDateTime slaDueAt,
        OffsetDateTime createdAt
) {
}
