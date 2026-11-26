package dd.conexiones.dd_ms_network.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NapPortAssignmentDTO(
        UUID id,
        UUID nodeId,
        UUID clientId,
        int portNumber,
        OffsetDateTime assignedAt
) {
}
