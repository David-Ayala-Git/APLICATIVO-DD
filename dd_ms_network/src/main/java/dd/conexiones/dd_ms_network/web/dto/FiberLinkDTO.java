package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.enums.FiberType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FiberLinkDTO(
        UUID id,
        String name,
        UUID fromNodeId,
        UUID toNodeId,
        FiberType fiberType,
        String color,
        List<FiberPointDTO> waypoints,
        OffsetDateTime createdAt
) {
}
