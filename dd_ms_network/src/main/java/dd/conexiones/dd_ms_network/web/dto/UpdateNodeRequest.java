package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.enums.NodeStatus;
import dd.conexiones.dd_ms_network.domain.model.enums.NodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateNodeRequest(
        @NotBlank String name,
        @NotNull NodeType type,
        @NotNull Double latitude,
        @NotNull Double longitude,
        NodeStatus status
) {
}
