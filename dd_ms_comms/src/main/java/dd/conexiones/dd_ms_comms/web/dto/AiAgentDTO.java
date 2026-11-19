package dd.conexiones.dd_ms_comms.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiAgentDTO(
        UUID id,
        String name,
        String channel,
        String systemPrompt,
        String tools,
        boolean enabled,
        OffsetDateTime createdAt
) {
}
