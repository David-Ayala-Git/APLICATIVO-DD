package dd.conexiones.dd_ms_comms.web.dto;

import dd.conexiones.dd_ms_comms.domain.model.enums.MessageDirection;
import dd.conexiones.dd_ms_comms.domain.model.enums.MessageStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WhatsappMessageDTO(
        UUID id,
        String phone,
        MessageDirection direction,
        String body,
        String templateName,
        MessageStatus status,
        OffsetDateTime sentAt,
        OffsetDateTime createdAt
) {
}
