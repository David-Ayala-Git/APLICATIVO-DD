package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.enums.NodeStatus;
import dd.conexiones.dd_ms_network.domain.model.enums.NodeType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NodeDTO(
        UUID id,
        String name,
        NodeType type,
        Double latitude,
        Double longitude,
        NodeStatus status,
        OffsetDateTime createdAt
) {
}
